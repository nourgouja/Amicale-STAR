package tn.star.Pfe.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.star.Pfe.dto.auth.UserResponse;
import tn.star.Pfe.entity.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "actif", target = "actif")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}