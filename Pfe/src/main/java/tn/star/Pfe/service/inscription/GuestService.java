package tn.star.Pfe.service.inscription;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.star.Pfe.dto.inscription.GuestRequest;
import tn.star.Pfe.dto.inscription.GuestResponse;
import tn.star.Pfe.entity.user.Adherent;
import tn.star.Pfe.entity.inscription.Guest;
import tn.star.Pfe.entity.inscription.Inscription;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.repository.inscription.GuestRepository;
import tn.star.Pfe.repository.inscription.InscriptionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final InscriptionRepository inscriptionRepository;

    public GuestResponse addGuest(Long inscriptionId, GuestRequest request, Adherent adherent) {
        Inscription inscription = getInscriptionForAdherent(inscriptionId, adherent);
        return save(new Guest(), inscription, request);
    }

    public List<GuestResponse> getGuests(Long inscriptionId, Adherent adherent) {
        getInscriptionForAdherent(inscriptionId, adherent);
        return mapList(guestRepository.findByInscriptionId(inscriptionId));
    }

    public GuestResponse updateGuest(Long inscriptionId, Long guestId, GuestRequest request, Adherent adherent) {
        Inscription inscription = getInscriptionForAdherent(inscriptionId, adherent);
        Guest guest = getGuestOfInscription(guestId, inscriptionId);
        return save(guest, inscription, request);
    }

    public void deleteGuest(Long inscriptionId, Long guestId, Adherent adherent) {
        getInscriptionForAdherent(inscriptionId, adherent);
        Guest guest = getGuestOfInscription(guestId, inscriptionId);
        guestRepository.delete(guest);
    }

    public GuestResponse addGuestAdmin(Long inscriptionId, GuestRequest request) {
        Inscription inscription = findInscriptionOrThrow(inscriptionId);
        return save(new Guest(), inscription, request);
    }

    public List<GuestResponse> getGuestsAdmin(Long inscriptionId) {
        findInscriptionOrThrow(inscriptionId);
        return mapList(guestRepository.findByInscriptionId(inscriptionId));
    }

    public GuestResponse updateGuestAdmin(Long inscriptionId, Long guestId, GuestRequest request) {
        Inscription inscription = findInscriptionOrThrow(inscriptionId);
        Guest guest = getGuestOfInscription(guestId, inscriptionId);
        return save(guest, inscription, request);
    }

    public void deleteGuestAdmin(Long inscriptionId, Long guestId) {
        findInscriptionOrThrow(inscriptionId);
        Guest guest = getGuestOfInscription(guestId, inscriptionId);
        guestRepository.delete(guest);
    }

    private Inscription getInscriptionForAdherent(Long inscriptionId, Adherent adherent) {
        return inscriptionRepository.findByIdAndAdherent(inscriptionId, adherent)
                .orElseThrow(() -> new NotFoundException("Inscription introuvable"));
    }

    private Inscription findInscriptionOrThrow(Long inscriptionId) {
        return inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new NotFoundException("Inscription introuvable"));
    }

    private Guest getGuestOfInscription(Long guestId, Long inscriptionId) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new NotFoundException("Invité introuvable"));
        if (!guest.getInscription().getId().equals(inscriptionId))
            throw new BadRequestException("Cet invité n'appartient pas à cette inscription");
        return guest;
    }

    private GuestResponse save(Guest guest, Inscription inscription, GuestRequest req) {
        guest.setInscription(inscription);
        guest.setNom(req.getNom());
        guest.setPrenom(req.getPrenom());
        guest.setAge(req.getAge());
        guest.setSexe(req.getSexe());
        return toResponse(guestRepository.save(guest));
    }

    private List<GuestResponse> mapList(List<Guest> guests) {
        return guests.stream().map(this::toResponse).toList();
    }

    public GuestResponse toResponse(Guest g) {
        return GuestResponse.builder()
                .id(g.getId())
                .inscriptionId(g.getInscription().getId())
                .nom(g.getNom())
                .prenom(g.getPrenom())
                .age(g.getAge())
                .sexe(g.getSexe())
                .createdAt(g.getCreatedAt())
                .build();
    }
}
