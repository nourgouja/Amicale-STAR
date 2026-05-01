package tn.star.Pfe.service.auth;

import tn.star.Pfe.dto.auth.login.AuthResponse;
import tn.star.Pfe.dto.auth.login.LoginRequest;

public interface IAuthService {
    AuthResponse login(LoginRequest request);
    void logout();
}
