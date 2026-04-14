package tn.star.Pfe.service.auth;

import tn.star.Pfe.security.UserPrincipal;

public interface IEcheanceAuthService {
    boolean canValidate(UserPrincipal principal, Long echeanceId);
}
