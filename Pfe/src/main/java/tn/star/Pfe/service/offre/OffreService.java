package tn.star.Pfe.service.offre;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.offre.OffreRequest;
import tn.star.Pfe.dto.offre.OffreResponse;
import tn.star.Pfe.entity.offre.Offre;
import tn.star.Pfe.entity.user.User;
import tn.star.Pfe.enums.OfferStatus;
import tn.star.Pfe.event.OffreCreatedEvent;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.mapper.OffreMapper;
import tn.star.Pfe.entity.user.Pole;
import tn.star.Pfe.entity.user.MembreBureau;
import tn.star.Pfe.entity.offre.OffreImage;
import tn.star.Pfe.repository.offer.OffreImageRepository;
import tn.star.Pfe.repository.offer.OffreRepository;
import tn.star.Pfe.repository.user.PoleRepository;
import tn.star.Pfe.repository.user.UserRepository;
import tn.star.Pfe.enums.TypeOffre;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OffreService implements IOffreService {

    private final OffreRepository offreRepository;
    private final PoleRepository poleRepository;
    private final OffreMapper offreMapper;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;
    private final OffreImageRepository offreImageRepository;


    public List<OffreResponse> listerOffresOuvertes() {
        return offreRepository.findByStatut(OfferStatus.OPEN)
                .stream()
                .map(offreMapper::toResponse)
                .toList();
    }

    public List<OffreResponse> listerOffresPubliques() {
        return offreRepository.findByStatutInOrType(
                        List.of(OfferStatus.OPEN, OfferStatus.CLOSED), TypeOffre.CONVENTION)
                .stream()
                .map(offreMapper::toResponse)
                .toList();
    }

    @Transactional
    public OffreResponse trouverParId(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable : " + id));
        offre.getImagesSupplementaires().size(); // force lazy load
        return offreMapper.toResponse(offre);
    }

    public List<OffreResponse> rechercherParTitre(String titre) {
        return offreRepository.findByTitreContainingIgnoreCase(titre)
                .stream()
                .map(offreMapper::toResponse)
                .toList();
    }

    @Transactional
    public OffreResponse creer(OffreRequest req, MultipartFile image, List<MultipartFile> imagesSupplementaires, String username) throws IOException {
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        Pole pole = null;
        if (currentUser instanceof MembreBureau mb && mb.getPole() != null) {
            pole = mb.getPole();
        } else if (req.getPoleId() != null) {
            pole = poleRepository.findById(req.getPoleId())
                    .orElseThrow(() -> new NotFoundException("Pôle introuvable : " + req.getPoleId()));
        } else if (req.getTypeOffre() != null) {
            pole = poleRepository.findFirstByTypesOffreContaining(req.getTypeOffre()).orElse(null);
        }

        Offre offre = Offre.builder()
                .titre(req.getTitre())
                .description(req.getDescription())
                .lieu(req.getLieu())
                .type(req.getTypeOffre())
                .dateDebut(req.getDateDebut())
                .dateFin(req.getDateFin())
                .prixParPersonne(req.getPrixParPersonne())
                .capaciteMax(req.getCapaciteMax() != null ? req.getCapaciteMax() : 0)
                .avantages(req.getAvantages())
                .lienExterne(req.getLienExterne())
                .statut(req.getStatut() != null ? req.getStatut() : OfferStatus.OPEN)
                .pole(pole)
                .createdBy(currentUser)
                .build();

        if (image != null && !image.isEmpty()) {
            offre.setImage(image.getBytes());
            offre.setImageNom(image.getOriginalFilename());
            offre.setImageType(image.getContentType());
        }

        validerParType(offre);
        Offre saved = offreRepository.save(offre);

        if (imagesSupplementaires != null) {
            for (MultipartFile extra : imagesSupplementaires) {
                if (extra != null && !extra.isEmpty()) {
                    offreImageRepository.save(OffreImage.builder()
                            .offre(saved)
                            .data(extra.getBytes())
                            .nom(extra.getOriginalFilename())
                            .type(extra.getContentType())
                            .build());
                }
            }
        }

        publisher.publishEvent(new OffreCreatedEvent(saved));
        return offreMapper.toResponse(offreRepository.findById(saved.getId()).orElse(saved));
    }

    @Transactional
    public OffreResponse uploadImage(Long id, MultipartFile image) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable"));
        try {
            offre.setImage(image.getBytes());
            offre.setImageNom(image.getOriginalFilename());
            offre.setImageType(image.getContentType());
        } catch (IOException e) {
            throw new BadRequestException("Erreur lecture image.");
        }
        return offreMapper.toResponse(offreRepository.save(offre));
    }

    @Transactional
    public OffreResponse modifier(Long id, OffreRequest.UpdateOffreRequest req) {

        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable : " + id));

        if (offre.getStatut() == OfferStatus.CANCELLED)
            throw new BadRequestException("Impossible de modifier une offre annulée.");

        if (req.getTitre() != null)
            offre.setTitre(req.getTitre());
        if (req.getDescription() != null)
            offre.setDescription(req.getDescription());
        if (req.getTypeOffre() != null)
            offre.setType(req.getTypeOffre());
        if (req.getCapaciteMax() != null)
            offre.setCapaciteMax(req.getCapaciteMax());
        if (req.getPrixParPersonne() != null)
            offre.setPrixParPersonne(req.getPrixParPersonne());
        if (req.getLieu() != null)
            offre.setLieu(req.getLieu());
        if (req.getDateDebut() != null)
            offre.setDateDebut(req.getDateDebut());
        if (req.getDateFin() != null)
            offre.setDateFin(req.getDateFin());
        validerParType(offre);
        return offreMapper.toResponse(offreRepository.save(offre));
    }

    @Transactional
    public OffreResponse modifierAvecImage(Long id, OffreRequest req, MultipartFile image) throws IOException {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable : " + id));

        if (offre.getStatut() == OfferStatus.CANCELLED)
            throw new BadRequestException("Impossible de modifier une offre annulée.");

        if (req.getTitre()           != null) offre.setTitre(req.getTitre());
        if (req.getDescription()     != null) offre.setDescription(req.getDescription());
        if (req.getTypeOffre()       != null) offre.setType(req.getTypeOffre());
        if (req.getLieu()            != null) offre.setLieu(req.getLieu());
        if (req.getDateDebut()       != null) offre.setDateDebut(req.getDateDebut());
        if (req.getDateFin()         != null) offre.setDateFin(req.getDateFin());
        if (req.getPrixParPersonne() != null) offre.setPrixParPersonne(req.getPrixParPersonne());
        if (req.getCapaciteMax()     != null) offre.setCapaciteMax(req.getCapaciteMax());
        if (req.getAvantages()       != null) offre.setAvantages(req.getAvantages());
        if (req.getLienExterne()     != null) offre.setLienExterne(req.getLienExterne());

        if (image != null && !image.isEmpty()) {
            offre.setImage(image.getBytes());
            offre.setImageNom(image.getOriginalFilename());
            offre.setImageType(image.getContentType());
        }

        validerParType(offre);
        return offreMapper.toResponse(offreRepository.save(offre));
    }

    @Transactional
    public OffreResponse fermer(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable : " + id));
        offre.setStatut(OfferStatus.CLOSED);
        return offreMapper.toResponse(offreRepository.save(offre));
    }

    @Transactional
    public void supprimer(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable : " + id));
        offre.setStatut(OfferStatus.CANCELLED);
        offreRepository.save(offre);
    }

    private void validerParType(Offre offre) {
        if (offre.getType() == null)
            throw new BadRequestException("Type d'offre obligatoire.");

        boolean dateOptional = offre.getType() == TypeOffre.CONVENTION;

        if (!dateOptional && offre.getDateDebut() == null)
            throw new BadRequestException("Date début obligatoire.");

        if (offre.getType() == TypeOffre.VOYAGE || offre.getType() == TypeOffre.SEJOUR) {
            if (offre.getDateFin() == null)
                throw new BadRequestException("Date fin obligatoire.");
            if (offre.getDateDebut() != null && !offre.getDateFin().isAfter(offre.getDateDebut()))
                throw new BadRequestException("Date fin doit être après date début.");
        }
    }

    @Transactional
    public OffreResponse publier(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable"));

        if (offre.getStatut() != OfferStatus.DRAFT)
            throw new BadRequestException("Seules les offres en brouillon peuvent être publiées. Statut actuel: " + offre.getStatut());

        validerParType(offre);
        offre.setStatut(OfferStatus.OPEN);
        return offreMapper.toResponse(offreRepository.save(offre));
    }

    @Transactional
    public OffreResponse archiver(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable"));
        offre.setStatut(OfferStatus.ARCHIVEE);
        return offreMapper.toResponse(offreRepository.save(offre));
    }

    public List<OffreResponse> listerToutesLesOffres() {
        return offreRepository.findAll()
                .stream()
                .map(offreMapper::toResponse)
                .toList();
    }

}