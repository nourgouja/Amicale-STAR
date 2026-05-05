package tn.star.Pfe.service.user;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.auth.create.CreateUserRequest;
import tn.star.Pfe.dto.auth.create.MembreBureauPublicResponse;
import tn.star.Pfe.dto.auth.create.UserResponse;
import tn.star.Pfe.dto.auth.login.ChangePasswordRequest;
import tn.star.Pfe.dto.auth.update.UpdateProfilRequest;
import tn.star.Pfe.entity.user.User;
import tn.star.Pfe.enums.Role;

import java.util.List;

public interface IUserService {
    Page<UserResponse> findAll(Role role, String search, int page, int size, boolean actif);
    User findById(Long id);
    UserResponse createUser(CreateUserRequest request);
    UserResponse uploadPhoto(Long id, MultipartFile photo);


    User updateUser(Long id, UpdateProfilRequest request);
    void deleteUser(Long id);
    User assignRole(Long id, Role role);
    User toggleUserStatus(Long id, boolean actif);
    void adminResetPassword(Long id);
    void forgotPasswordByEmail(String email);
    void changePassword(Long userId, ChangePasswordRequest request);
    List<MembreBureauPublicResponse> findMembresBureau();
}
