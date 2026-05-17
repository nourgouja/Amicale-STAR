package tn.star.Pfe.service.sondage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.sondage.OptionSondageResponse;
import tn.star.Pfe.dto.sondage.SondageRequest;
import tn.star.Pfe.dto.sondage.SondageResponse;
import tn.star.Pfe.dto.sondage.UpdateSondageRequest;
import tn.star.Pfe.entity.sondage.OptionSondage;
import tn.star.Pfe.entity.sondage.Sondage;
import tn.star.Pfe.entity.user.User;
import tn.star.Pfe.entity.sondage.VoteSondage;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.enums.LifecycleStatus;
import tn.star.Pfe.event.SondageClosedEvent;
import tn.star.Pfe.event.SondageOpenedEvent;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.exceptions.ForbiddenException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.repository.sondage.SondageRepository;
import tn.star.Pfe.repository.user.UserRepository;
import tn.star.Pfe.repository.sondage.VoteSondageRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SondageService implements ISondageService {

    private static final long MAX_IMAGE_SIZE = 1024 * 1024; // 1MB

    private final SondageRepository sondageRepository;
    private final VoteSondageRepository voteSondageRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public SondageResponse creer(SondageRequest req, MultipartFile image1, MultipartFile image2, String username) throws IOException {
        User creator = findUser(username);

        OptionSondage opt1 = buildOption(req.getOption1(), image1, 0);
        OptionSondage opt2 = buildOption(req.getOption2(), image2, 1);

        Sondage sondage = Sondage.builder()
                .titre(req.getTitre())
                .statut(LifecycleStatus.OPEN)
                .createdBy(creator)
                .build();
        opt1.setSondage(sondage);
        opt2.setSondage(sondage);
        sondage.getOptions().add(opt1);
        sondage.getOptions().add(opt2);

        Sondage saved = sondageRepository.save(sondage);
        eventPublisher.publishEvent(new SondageOpenedEvent(this, saved));
        log.info("Sondage créé et activé: id={} par {}", saved.getId(), username);
        return toResponse(saved, creator);
    }

    @Override
    public SondageResponse modifier(Long id, UpdateSondageRequest req, MultipartFile image1, MultipartFile image2, String username) throws IOException {
        Sondage sondage = findSondage(id);
        User user = findUser(username);
        checkOwnerOrAdmin(sondage, user);

        if (sondage.getStatut() != LifecycleStatus.DRAFT)
            throw new BadRequestException("Seuls les sondages en DRAFT peuvent être modifiés");

        if (req.getTitre() != null) sondage.setTitre(req.getTitre());

        List<OptionSondage> options = sondage.getOptions();
        if (!options.isEmpty() && req.getOption1() != null)
            updateOption(options.get(0), req.getOption1(), image1);
        if (options.size() > 1 && req.getOption2() != null)
            updateOption(options.get(1), req.getOption2(), image2);

        Sondage saved = sondageRepository.save(sondage);
        log.info("Sondage modifié: id={} par {}", id, username);
        return toResponse(saved, user);
    }

    @Override
    public SondageResponse activer(Long id, String username) {
        Sondage sondage = findSondage(id);
        User user = findUser(username);
        checkOwnerOrAdmin(sondage, user);

        if (sondage.getStatut() != LifecycleStatus.DRAFT)
            throw new BadRequestException("Seul un sondage DRAFT peut être activé");

        sondage.setStatut(LifecycleStatus.OPEN);
        Sondage saved = sondageRepository.save(sondage);
        eventPublisher.publishEvent(new SondageOpenedEvent(this, saved));
        log.info("Sondage activé: id={} par {}", id, username);
        return toResponse(saved, user);
    }

    @Override
    public SondageResponse fermer(Long id, String username) {
        Sondage sondage = findSondage(id);
        User user = findUser(username);
        checkOwnerOrAdmin(sondage, user);

        if (sondage.getStatut() != LifecycleStatus.OPEN)
            throw new BadRequestException("Seul un sondage ACTIVE peut être fermé");

        sondage.setStatut(LifecycleStatus.CLOSED);
        sondage.setClosedAt(LocalDateTime.now());
        Sondage saved = sondageRepository.save(sondage);

        eventPublisher.publishEvent(new SondageClosedEvent(this, saved, username));
        log.info("Sondage fermé: id={} par {}", id, username);
        return toResponse(saved, user);
    }

    @Override
    public SondageResponse archiver(Long id, String username) {
        Sondage sondage = findSondage(id);
        User user = findUser(username);
        checkOwnerOrAdmin(sondage, user);

        if (sondage.getStatut() != LifecycleStatus.CLOSED)
            throw new BadRequestException("Seul un sondage CLOSED peut être archivé");

        sondage.setStatut(LifecycleStatus.ARCHIVED);
        Sondage saved = sondageRepository.save(sondage);
        log.info("Sondage archivé: id={} par {}", id, username);
        return toResponse(saved, user);
    }

    @Override
    public void supprimer(Long id, String username) {
        Sondage sondage = findSondage(id);
        User user = findUser(username);
        checkOwnerOrAdmin(sondage, user);

        sondageRepository.delete(sondage);
        log.info("Sondage supprimé: id={} par {}", id, username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SondageResponse> lister(String username) {
        User user = findUser(username);
        boolean isPrivileged = isAdminOrBureau(user);

        List<Sondage> sondages = isPrivileged
                ? sondageRepository.findAllByOrderByCreatedAtDesc()
                : sondageRepository.findVisibleToAdherents();

        log.info("Listage sondages: privileged={}, count={}, user={}", isPrivileged, sondages.size(), username);
        return sondages.stream()
                .map(s -> toResponse(s, user))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SondageResponse trouverParId(Long id, String username) {
        Sondage sondage = findSondage(id);
        User user = findUser(username);

        if (!isAdminOrBureau(user)
                && sondage.getStatut() != LifecycleStatus.OPEN
                && sondage.getStatut() != LifecycleStatus.CLOSED)
            throw new NotFoundException("Sondage non trouvé");

        log.info("Sondage récupéré: id={} par {}", id, username);
        return toResponse(sondage, user);
    }

    @Override
    public SondageResponse voter(Long sondageId, Long optionId, String username) {
        Sondage sondage = findSondage(sondageId);
        User user = findUser(username);

        if (sondage.getStatut() != LifecycleStatus.OPEN)
            throw new BadRequestException("Impossible de voter sur un sondage qui n'est pas ACTIVE");

        OptionSondage option = sondage.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Option non trouvée"));

        if (voteSondageRepository.existsBySondageIdAndAdherentId(sondageId, user.getId()))
            throw new BadRequestException("Vous avez déjà voté pour ce sondage");

        VoteSondage vote = VoteSondage.builder()
                .sondage(sondage)
                .adherent(user)
                .option(option)
                .build();
        voteSondageRepository.save(vote);

        log.info("Vote enregistré: sondage={}, option={}, user={}", sondageId, optionId, username);
        return toResponse(sondage, user);
    }

    // --- Helpers ---

    private User findUser(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
    }

    private Sondage findSondage(Long id) {
        return sondageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sondage non trouvé"));
    }

    private boolean isAdminOrBureau(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.MEMBRE_BUREAU;
    }

    private void checkOwnerOrAdmin(Sondage sondage, User user) {
        if (!sondage.getCreatedBy().getId().equals(user.getId()) && !isAdminOrBureau(user))
            throw new ForbiddenException("Accès non autorisé à ce sondage");
    }

    private OptionSondage buildOption(SondageRequest.OptionRequest optReq, MultipartFile image, int ordre) throws IOException {
        OptionSondage option = new OptionSondage();
        option.setTitre(optReq.getTitre());
        option.setDescription(optReq.getDescription());
        option.setOrdre(ordre);
        applyImage(option, image);
        return option;
    }

    private void updateOption(OptionSondage option, UpdateSondageRequest.OptionUpdateRequest req, MultipartFile image) throws IOException {
        if (req.getTitre() != null) option.setTitre(req.getTitre());
        if (req.getDescription() != null) option.setDescription(req.getDescription());
        if (image != null && !image.isEmpty()) applyImage(option, image);
    }

    private void applyImage(OptionSondage option, MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) return;
        if (image.getSize() > MAX_IMAGE_SIZE)
            throw new BadRequestException("L'image de l'option \"" + option.getTitre() + "\" dépasse la limite de 1MB");
        option.setImage(image.getBytes());
        option.setImageNom(image.getOriginalFilename());
        option.setImageType(image.getContentType());
    }

    private SondageResponse toResponse(Sondage s, User currentUser) {
        var existingVote = voteSondageRepository.findBySondageIdAndAdherentId(s.getId(), currentUser.getId());
        boolean hasVoted = existingVote.isPresent();
        Long votedOptionId = hasVoted ? existingVote.get().getOption().getId() : null;

        long totalVotes = voteSondageRepository.countBySondageId(s.getId());

        List<Long> optionIds = s.getOptions().stream()
                .map(OptionSondage::getId)
                .collect(Collectors.toList());
        Map<Long, Long> voteCounts = optionIds.isEmpty()
                ? Map.of()
                : voteSondageRepository.countsByOptionIds(optionIds);

        List<OptionSondageResponse> optionResponses = s.getOptions().stream()
                .map(o -> OptionSondageResponse.builder()
                        .id(o.getId())
                        .titre(o.getTitre())
                        .description(o.getDescription())
                        .imageBase64(o.getImage() != null ? Base64.getEncoder().encodeToString(o.getImage()) : null)
                        .imageType(o.getImageType())
                        .ordre(o.getOrdre())
                        .voteCount(voteCounts.getOrDefault(o.getId(), 0L))
                        .build())
                .collect(Collectors.toList());

        return SondageResponse.builder()
                .id(s.getId())
                .titre(s.getTitre())
                .statut(s.getStatut().name())
                .createdByNom(s.getCreatedBy().getNom())
                .createdByPrenom(s.getCreatedBy().getPrenom())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .closedAt(s.getClosedAt())
                .options(optionResponses)
                .hasVoted(hasVoted)
                .votedOptionId(votedOptionId)
                .totalVotes(totalVotes)
                .build();
    }
}
