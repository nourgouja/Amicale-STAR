package tn.star.Pfe.service.sondage;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.sondage.*;
import tn.star.Pfe.entity.*;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.enums.StatutSondage;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.repository.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SondageService implements ISondageService {

    private static final long MAX_IMAGE_BYTES = 1_000_000L;

    private final SondageRepository sondageRepository;
    private final OptionSondageRepository optionSondageRepository;
    private final VoteSondageRepository voteSondageRepository;
    private final UserRepository userRepository;

    // ── helpers ──────────────────────────────────────────────────────────────

    private User loadUser(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
    }

    private Adherent requirePresident(User user) {
        if (!(user instanceof Adherent a) || !"president".equalsIgnoreCase(a.getPoste()))
            throw new BadRequestException("Seul le président peut effectuer cette action.");
        return a;
    }

    private Adherent requireVoter(User user) {
        if (!(user instanceof Adherent a))
            throw new BadRequestException("Seuls les adhérents peuvent voter.");
        if ("president".equalsIgnoreCase(a.getPoste()))
            throw new BadRequestException("Le président vote via son compte personnel adhérent.");
        return a;
    }

    private boolean canSeeAllSurveys(User user) {
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.MEMBRE_BUREAU) return true;
        return user instanceof Adherent a && "president".equalsIgnoreCase(a.getPoste());
    }

    private void validateImage(MultipartFile file, int optionOrdre) {
        if (file != null && !file.isEmpty() && file.getSize() > MAX_IMAGE_BYTES)
            throw new BadRequestException("L'image de l'option " + optionOrdre + " ne peut pas dépasser 1 Mo.");
    }

    private OptionSondage buildOption(Sondage sondage, SondageRequest.OptionRequest req, MultipartFile image, int ordre) throws IOException {
        OptionSondage opt = OptionSondage.builder()
                .sondage(sondage)
                .titre(req.getTitre())
                .description(req.getDescription())
                .ordre(ordre)
                .build();
        applyImage(opt, image);
        return opt;
    }

    private void applyImage(OptionSondage opt, MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            opt.setImage(image.getBytes());
            opt.setImageNom(image.getOriginalFilename());
            opt.setImageType(image.getContentType());
        }
    }

    private SondageResponse toResponse(Sondage s, Long adherentId) {
        VoteSondage userVote = adherentId != null
                ? voteSondageRepository.findBySondageIdAndAdherentId(s.getId(), adherentId).orElse(null)
                : null;

        boolean isClosed = s.getStatut() == StatutSondage.CLOSED;

        List<OptionSondageResponse> optionResponses = s.getOptions().stream()
                .map(opt -> OptionSondageResponse.builder()
                        .id(opt.getId())
                        .titre(opt.getTitre())
                        .description(opt.getDescription())
                        .imageBase64(opt.getImage() != null ? Base64.getEncoder().encodeToString(opt.getImage()) : null)
                        .imageType(opt.getImageType())
                        .ordre(opt.getOrdre())
                        .voteCount(isClosed ? voteSondageRepository.countByOptionId(opt.getId()) : null)
                        .build())
                .toList();

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
                .hasVoted(userVote != null)
                .votedOptionId(userVote != null ? userVote.getOption().getId() : null)
                .totalVotes(isClosed ? voteSondageRepository.countBySondageId(s.getId()) : 0)
                .build();
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SondageResponse creer(SondageRequest req, MultipartFile image1, MultipartFile image2, String username) throws IOException {
        Adherent president = requirePresident(loadUser(username));

        validateImage(image1, 1);
        validateImage(image2, 2);

        Sondage sondage = sondageRepository.save(
                Sondage.builder().titre(req.getTitre()).createdBy(president).build());

        optionSondageRepository.save(buildOption(sondage, req.getOption1(), image1, 1));
        optionSondageRepository.save(buildOption(sondage, req.getOption2(), image2, 2));

        Sondage saved = sondageRepository.findById(sondage.getId()).orElse(sondage);
        return toResponse(saved, president.getId());
    }

    @Override
    @Transactional
    public SondageResponse modifier(Long id, UpdateSondageRequest req, MultipartFile image1, MultipartFile image2, String username) throws IOException {
        User user = loadUser(username);
        Adherent president = requirePresident(user);

        Sondage sondage = sondageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sondage introuvable : " + id));

        if (sondage.getStatut() != StatutSondage.DRAFT)
            throw new BadRequestException("Seuls les sondages en brouillon peuvent être modifiés.");

        if (!sondage.getCreatedBy().getId().equals(president.getId()))
            throw new BadRequestException("Vous n'êtes pas autorisé à modifier ce sondage.");

        if (req.getTitre() != null) sondage.setTitre(req.getTitre());

        List<OptionSondage> options = optionSondageRepository.findBySondageIdOrderByOrdre(id);

        if (options.size() >= 1) {
            OptionSondage opt1 = options.get(0);
            UpdateSondageRequest.OptionUpdateRequest r1 = req.getOption1();
            if (r1 != null) {
                if (r1.getTitre() != null) opt1.setTitre(r1.getTitre());
                if (r1.getDescription() != null) opt1.setDescription(r1.getDescription());
            }
            validateImage(image1, 1);
            applyImage(opt1, image1);
            optionSondageRepository.save(opt1);
        }
        if (options.size() >= 2) {
            OptionSondage opt2 = options.get(1);
            UpdateSondageRequest.OptionUpdateRequest r2 = req.getOption2();
            if (r2 != null) {
                if (r2.getTitre() != null) opt2.setTitre(r2.getTitre());
                if (r2.getDescription() != null) opt2.setDescription(r2.getDescription());
            }
            validateImage(image2, 2);
            applyImage(opt2, image2);
            optionSondageRepository.save(opt2);
        }

        return toResponse(sondageRepository.save(sondage), president.getId());
    }

    @Override
    @Transactional
    public SondageResponse activer(Long id, String username) {
        User user = loadUser(username);
        Adherent president = requirePresident(user);

        Sondage sondage = sondageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sondage introuvable : " + id));

        if (!sondage.getCreatedBy().getId().equals(president.getId()))
            throw new BadRequestException("Vous n'êtes pas autorisé à activer ce sondage.");

        if (sondage.getStatut() != StatutSondage.DRAFT)
            throw new BadRequestException("Seuls les sondages en brouillon peuvent être activés.");

        if (sondage.getOptions().stream().anyMatch(o -> o.getImage() == null))
            throw new BadRequestException("Les deux options doivent avoir une image avant activation.");

        sondage.setStatut(StatutSondage.ACTIVE);
        return toResponse(sondageRepository.save(sondage), president.getId());
    }

    @Override
    @Transactional
    public SondageResponse fermer(Long id, String username) {
        User user = loadUser(username);

        Sondage sondage = sondageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sondage introuvable : " + id));

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isCreator = user instanceof Adherent a && sondage.getCreatedBy().getId().equals(a.getId());
        if (!isAdmin && !isCreator)
            throw new BadRequestException("Seul le créateur ou un administrateur peut fermer ce sondage.");

        if (sondage.getStatut() != StatutSondage.ACTIVE)
            throw new BadRequestException("Seuls les sondages actifs peuvent être fermés.");

        sondage.setStatut(StatutSondage.CLOSED);
        sondage.setClosedAt(LocalDateTime.now());

        Long adherentId = user instanceof Adherent a ? a.getId() : null;
        return toResponse(sondageRepository.save(sondage), adherentId);
    }

    @Override
    @Transactional
    public SondageResponse archiver(Long id, String username) {
        User user = loadUser(username);
        Adherent president = requirePresident(user);

        Sondage sondage = sondageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sondage introuvable : " + id));

        if (!sondage.getCreatedBy().getId().equals(president.getId()))
            throw new BadRequestException("Vous n'êtes pas autorisé à archiver ce sondage.");

        if (sondage.getStatut() != StatutSondage.CLOSED)
            throw new BadRequestException("Seuls les sondages fermés peuvent être archivés.");

        sondage.setStatut(StatutSondage.ARCHIVED);
        return toResponse(sondageRepository.save(sondage), president.getId());
    }

    @Override
    @Transactional
    public void supprimer(Long id, String username) {
        User user = loadUser(username);
        Adherent president = requirePresident(user);

        Sondage sondage = sondageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sondage introuvable : " + id));

        if (!sondage.getCreatedBy().getId().equals(president.getId()))
            throw new BadRequestException("Vous n'êtes pas autorisé à supprimer ce sondage.");

        if (sondage.getStatut() != StatutSondage.DRAFT)
            throw new BadRequestException("Seuls les sondages en brouillon peuvent être supprimés.");

        sondageRepository.delete(sondage);
    }

    @Override
    @Transactional
    public List<SondageResponse> lister(String username) {
        User user = loadUser(username);
        Long adherentId = user instanceof Adherent a ? a.getId() : null;

        List<Sondage> sondages = canSeeAllSurveys(user)
                ? sondageRepository.findAllByOrderByCreatedAtDesc()
                : sondageRepository.findVisibleToAdherents();

        return sondages.stream().map(s -> toResponse(s, adherentId)).toList();
    }

    @Override
    @Transactional
    public SondageResponse trouverParId(Long id, String username) {
        User user = loadUser(username);

        Sondage sondage = sondageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sondage introuvable : " + id));

        if (!canSeeAllSurveys(user)) {
            if (sondage.getStatut() == StatutSondage.DRAFT || sondage.getStatut() == StatutSondage.ARCHIVED)
                throw new NotFoundException("Sondage introuvable : " + id);
        }

        sondage.getOptions().size(); // trigger lazy load
        Long adherentId = user instanceof Adherent a ? a.getId() : null;
        return toResponse(sondage, adherentId);
    }

    @Override
    @Transactional
    public SondageResponse voter(Long sondageId, Long optionId, String username) {
        User user = loadUser(username);
        Adherent adherent = requireVoter(user);

        Sondage sondage = sondageRepository.findById(sondageId)
                .orElseThrow(() -> new NotFoundException("Sondage introuvable."));

        if (sondage.getStatut() != StatutSondage.ACTIVE)
            throw new BadRequestException("Ce sondage n'est pas ouvert au vote.");

        if (voteSondageRepository.existsBySondageIdAndAdherentId(sondageId, adherent.getId()))
            throw new BadRequestException("Vous avez déjà voté pour ce sondage.");

        OptionSondage option = optionSondageRepository.findById(optionId)
                .orElseThrow(() -> new NotFoundException("Option introuvable."));

        if (!option.getSondage().getId().equals(sondageId))
            throw new BadRequestException("Cette option n'appartient pas à ce sondage.");

        voteSondageRepository.save(VoteSondage.builder()
                .sondage(sondage)
                .adherent(adherent)
                .option(option)
                .build());

        sondage.getOptions().size(); // trigger lazy load
        return toResponse(sondage, adherent.getId());
    }
}
