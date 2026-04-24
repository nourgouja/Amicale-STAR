package tn.star.Pfe.mapper;

import org.springframework.stereotype.Component;
import tn.star.Pfe.dto.auth.UserResponse;
import tn.star.Pfe.entity.Adherent;
import tn.star.Pfe.entity.MembreBureau;
import tn.star.Pfe.entity.User;

import java.util.Base64;
import java.util.List;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        String posteMembre   = null;
        Long   poleId        = null;
        String poleNom       = null;
        String poleTypeOffre = null;
        String matriculeStar = null;

        if (user instanceof MembreBureau mb) {
            posteMembre = mb.getPoste() != null ? mb.getPoste().name() : null;
            if (mb.getPole() != null) {
                poleId        = mb.getPole().getId();
                poleNom       = mb.getPole().getNom();
                poleTypeOffre = mb.getPole().getTypeOffre() != null
                        ? mb.getPole().getTypeOffre().name() : null;
            }
        } else if (user instanceof Adherent a) {
            matriculeStar = a.getMatriculeStar();
        }

        String photoBase64 = null;
        if (user.getPhoto() != null) {
            photoBase64 = Base64.getEncoder().encodeToString(user.getPhoto());
        }

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getRole() != null ? user.getRole().name() : null,
                user.isActif(),
                user.getTelephone(),
                matriculeStar,
                posteMembre,
                poleId,
                poleNom,
                poleTypeOffre,
                user.getCreatedAt(),
                photoBase64,
                user.getPhotoType()
        );
    }

    public List<UserResponse> toResponseList(List<User> users) {
        if (users == null) return null;
        return users.stream().map(this::toResponse).toList();
    }
}
