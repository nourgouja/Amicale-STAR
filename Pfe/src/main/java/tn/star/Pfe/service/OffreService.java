package tn.star.Pfe.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.offre.OffreRequest;
import tn.star.Pfe.dto.offre.OffreResponse;
import tn.star.Pfe.entity.Offre;
import tn.star.Pfe.entity.Pole;
import tn.star.Pfe.enums.StatutOffre;
import tn.star.Pfe.enums.TypeOffre;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.mapper.OffreMapper;
import tn.star.Pfe.repository.OffreRepository;
import tn.star.Pfe.repository.PoleRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OffreService {

    private final OffreRepository offreRepository;
    private final PoleRepository poleRepository;
    private final OffreMapper offreMapper;

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
    public OffreResponse creer(OffreRequest req, MultipartFile image) throws IOException {

        Pole pole = null;
        if (req.getPoleId() != null) {
            pole = poleRepository.findById(req.getPoleId())
                    .orElseThrow(() -> new NotFoundException("Pôle introuvable"));
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
                .modePaiement(req.getModePaiement())
                .avantages(req.getAvantages())
                .pole(pole)
                .statut(StatutOffre.BROUILLON)
                .build();

        if (image != null && !image.isEmpty()) {
            offre.setImage(image.getBytes());
            offre.setImageNom(image.getOriginalFilename());
            offre.setImageType(image.getContentType());
        }

        validerParType(offre);

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
        if (offre.getPrixParPersonne() != null && offre.getPrixParPersonne().compareTo(BigDecimal.ZERO) != 0)
            offre.setPrixParPersonne(req.getPrixParPersonne());
        if (req.getLieu() != null)
            offre.setLieu(req.getLieu());
        if (req.getDateDebut() != null)
            offre.setDateDebut(req.getDateDebut());
        if (req.getDateFin() != null)
            offre.setDateFin(req.getDateFin());

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
        if (!offreRepository.existsById(id))
            throw new NotFoundException("Offre introuvable : " + id);
        offreRepository.deleteById(id);
    }

    private void validerParType(Offre offre) {
        switch (offre.getType()) {

            case EVENEMENT, LOISIRS, ACTIVITE-> {
                if (offre.getDateFin() != null
                        && offre.getDateFin().isBefore(offre.getDateDebut())) {
                    throw new BadRequestException("Date fin invalide pour ce type d'offre.");
                }
            }

            case VOYAGE, SEJOUR -> {
                if (offre.getDateFin() == null) {
                    throw new BadRequestException("Date fin obligatoire pour VOYAGE / SEJOUR.");
                }
                if (!offre.getDateFin().isAfter(offre.getDateDebut())) {
                    throw new BadRequestException("Date fin doit être après date début.");
                }
            }

            case CONVENTION -> {
                if (offre.getPrixParPersonne() != null && offre.getPrixParPersonne().compareTo(BigDecimal.ZERO) != 0) {
                    throw new BadRequestException("Convention n'a pas de prix.");
                }
                if (offre.getImage() == null) {
                    throw new BadRequestException("Logo obligatoire pour une convention.");
                }
            }
        }
    }

    @Transactional
    public OffreResponse publier(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable"));

        if (offre.getStatut() != StatutOffre.BROUILLON)
            throw new BadRequestException(
                    "Seules les offres en brouillon peuvent être publiées. Statut actuel: "
                            + offre.getStatut());

        offre.setStatut(StatutOffre.OUVERTE);
        return offreMapper.toResponse(offreRepository.save(offre));
    }

    @Transactional
    public OffreResponse archiver(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable"));

        if (offre.getStatut() == StatutOffre.BROUILLON)
            throw new BadRequestException("Un brouillon doit être publié avant d'être archivé.");

        offre.setStatut(StatutOffre.ARCHIVEE);
        return offreMapper.toResponse(offreRepository.save(offre));
    }
}