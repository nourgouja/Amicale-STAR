package tn.star.Pfe.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import tn.star.Pfe.dto.auth.UserResponse;
import tn.star.Pfe.entity.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-25T19:58:03+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260101-2150, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        boolean actif = false;
        Long id = null;
        String email = null;
        String nom = null;
        String prenom = null;
        String role = null;
        LocalDateTime createdAt = null;

        actif = user.isActif();
        id = user.getId();
        email = user.getEmail();
        nom = user.getNom();
        prenom = user.getPrenom();
        if ( user.getRole() != null ) {
            role = user.getRole().name();
        }
        createdAt = user.getCreatedAt();

        UserResponse userResponse = new UserResponse( id, email, nom, prenom, role, actif, createdAt );

        return userResponse;
    }

    @Override
    public List<UserResponse> toResponseList(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<UserResponse> list = new ArrayList<UserResponse>( users.size() );
        for ( User user : users ) {
            list.add( toResponse( user ) );
        }

        return list;
    }
}
