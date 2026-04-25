package tn.star.Pfe.service.user;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.auth.*;
import tn.star.Pfe.entity.User;
import tn.star.Pfe.enums.Role;

import java.util.List;

public interface IUserService {
    Page<UserResponse> findAll(Role role, String search, int page, int size);
    User findById(Long id);
    UserResponse createUser(CreateUserRequest request);
    User updateUser(Long id, UpdateProfilRequest request);
    void deleteUser(Long id);
    User assignRole(Long id, Role role);
    User toggleUserStatus(Long id, boolean actif);
    void adminResetPassword(Long id);
    void forgotPasswordByEmail(String email);
    void changePassword(Long userId, ChangePasswordRequest request);
    void demanderAdhesion(DemandeRequest request);
    List<DemandeAdhesionResponse> findDemandesPending();
    void approuverDemande(Long id);
    void rejeterDemande(Long id);
    UserResponse uploadPhoto(Long id, MultipartFile photo);
}
