package tn.star.Pfe.service.auth;

import tn.star.Pfe.dto.auth.AuthResponse;
import tn.star.Pfe.dto.auth.LoginRequest;

public interface IAuthService {
    AuthResponse login(LoginRequest request);
    void logout();
}
