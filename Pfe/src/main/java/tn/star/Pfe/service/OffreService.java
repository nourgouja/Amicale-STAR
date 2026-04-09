package tn.star.Pfe.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.offre.OffreRequest;
import tn.star.Pfe.dto.offre.OffreResponse;
import tn.star.Pfe.entity.Offre;
import tn.star.Pfe.enums.StatutOffre;
import tn.star.Pfe.enums.TypeOffre;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.mapper.OffreMapper;
import tn.star.Pfe.repository.OffreRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OffreService {

    private final OffreRepository offreRepository;
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
    public OffreResponse creer(String titre, String description, TypeOffre typeOffre, LocalDate dateDebut, LocalDate dateFin, int capaciteMax, double prixParPersonne, String lieu, MultipartFile image) throws IOException {

        if (!dateFin.isAfter(dateDebut))
            throw new BadRequestException("La date de fin doit être après la date de début.");

        Offre offre = Offre.builder()
                .titre(titre)
                .description(description)
                .type(typeOffre)
                .statut(StatutOffre.OUVERTE)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .capaciteMax(capaciteMax)
                .prixParPersonne(prixParPersonne)
                .lieu(lieu)
                .build();

        if (image != null && !image.isEmpty()) {
            offre.setImage(image.getBytes());
            offre.setImageNom(image.getOriginalFilename());
            offre.setImageType(image.getContentType());
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
        if (req.getPrixParPersonne() != 0.0)
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
}