package tn.star.Pfe.service.offre;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.offre.OffreRequest;
import tn.star.Pfe.dto.offre.OffreResponse;
import tn.star.Pfe.entity.Offre;
import tn.star.Pfe.entity.User;
import tn.star.Pfe.enums.PosteBureau;
import tn.star.Pfe.enums.StatutOffre;
import tn.star.Pfe.event.OffreCreatedEvent;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.mapper.OffreMapper;
import tn.star.Pfe.entity.Pole;
import tn.star.Pfe.entity.MembreBureau;
import tn.star.Pfe.repository.OffreRepository;
import tn.star.Pfe.repository.PoleRepository;
import tn.star.Pfe.repository.UserRepository;

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


    public List<OffreResponse> listerOffresOuvertes() {
        return offreRepository.findByStatut(StatutOffre.OUVERTE)
                .stream()
                .map(offreMapper::toResponse)
                .toList();
    }

    public OffreResponse trouverParId(Long id) {
        return offreMapper.toResponse(
                offreRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Offre introuvable : " + id))
        );
    }

    public List<OffreResponse> rechercherParTitre(String titre) {
        return offreRepository.findByTitreContainingIgnoreCase(titre)
                .stream()
                .map(offreMapper::toResponse)
                .toList();
    }

    @Transactional
    public OffreResponse creer(OffreRequest req, MultipartFile image, String username) throws IOException {
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

//        Pole pole = poleRepository.findById(req.getPoleId())
//                .orElseThrow(() -> new NotFoundException("Pôle introuvable : " + req.getPoleId()));

//        if (currentUser instanceof MembreBureau membreBureau
//                && membreBureau.getPoste() == PosteBureau.RESPONSABLE_POLE
//                && !pole.equals(membreBureau.getPole())) {
//            throw new BadRequestException("Non autorisé pour ce pôle.");
//        }
        Offre offre = Offre.builder()
                .titre(req.getTitre())
                .description(req.getDescription())
                .lieu(req.getLieu())
                .type(req.getTypeOffre())
                .dateDebut(req.getDateDebut())
                .dateFin(req.getDateFin())
                .prixParPersonne(req.getPrixParPersonne())
                .capaciteMax(req.getCapaciteMax() != null ? req.getCapaciteMax() : 0)
                .modePaiement(req.getModePaiement())
                .avantages(req.getAvantages())
                .statut(req.getStatut() != null ? req.getStatut() : StatutOffre.OUVERTE)
                .build();

        if (image != null && !image.isEmpty()) {
            offre.setImage(image.getBytes());
            offre.setImageNom(image.getOriginalFilename());
            offre.setImageType(image.getContentType());
        }

        validerParType(offre);
        Offre saved = offreRepository.save(offre);
        publisher.publishEvent(new OffreCreatedEvent(saved));
        return offreMapper.toResponse(saved);
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

        if (offre.getStatut() == StatutOffre.ANNULEE)
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

        if (offre.getStatut() == StatutOffre.ANNULEE)
            throw new BadRequestException("Impossible de modifier une offre annulée.");

        if (req.getTitre()           != null) offre.setTitre(req.getTitre());
        if (req.getDescription()     != null) offre.setDescription(req.getDescription());
        if (req.getTypeOffre()       != null) offre.setType(req.getTypeOffre());
        if (req.getLieu()            != null) offre.setLieu(req.getLieu());
        if (req.getDateDebut()       != null) offre.setDateDebut(req.getDateDebut());
        if (req.getDateFin()         != null) offre.setDateFin(req.getDateFin());
        if (req.getPrixParPersonne() != null) offre.setPrixParPersonne(req.getPrixParPersonne());
        if (req.getCapaciteMax()     != null) offre.setCapaciteMax(req.getCapaciteMax());
        if (req.getModePaiement()    != null) offre.setModePaiement(req.getModePaiement());
        if (req.getAvantages()       != null) offre.setAvantages(req.getAvantages());

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
        offre.setStatut(StatutOffre.FERMEE);
        return offreMapper.toResponse(offreRepository.save(offre));
    }

    @Transactional
    public void supprimer(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable : " + id));
        offre.setStatut(StatutOffre.ANNULEE);
        offreRepository.save(offre);
    }

    private void validerParType(Offre offre) {
        if (offre.getType() == null)
            throw new BadRequestException("Type d'offre obligatoire.");

        if (offre.getDateDebut() == null)
            throw new BadRequestException("Date début obligatoire.");

        switch (offre.getType()) {

            case VOYAGE, SEJOUR -> {
                if (offre.getDateFin() == null)
                    throw new BadRequestException("Date fin obligatoire.");

                if (!offre.getDateFin().isAfter(offre.getDateDebut()))
                    throw new BadRequestException("Date fin doit être après date début.");
            }
        }
    }

    @Transactional
    public OffreResponse publier(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable"));

        if (offre.getStatut() != StatutOffre.BROUILLON)
            throw new BadRequestException("Seules les offres en brouillon peuvent être publiées. Statut actuel: " + offre.getStatut());

        validerParType(offre);
        offre.setStatut(StatutOffre.OUVERTE);
        return offreMapper.toResponse(offreRepository.save(offre));
    }

    @Transactional
    public OffreResponse archiver(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable"));
        offre.setStatut(StatutOffre.ARCHIVEE);
        return offreMapper.toResponse(offreRepository.save(offre));
    }

    public List<OffreResponse> listerToutesLesOffres() {
        return offreRepository.findAll()
                .stream()
                .map(offreMapper::toResponse)
                .toList();
    }

}