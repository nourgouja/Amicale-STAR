package tn.star.Pfe.dto.auth.login;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String currentPassword;

    @NotBlank(message = "ne doit pas être vide")
    private String newPassword;

    @NotBlank(message = "ne doit pas être vide")
    private String confirmPassword;

    @AssertTrue(message = "Les mots de passe ne correspondent pas")
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}