package tn.star.Pfe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.star.Pfe.service.email.IEmailService;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TestController {

    private final IEmailService emailService;

    @PostMapping("/email")
    public ResponseEntity<String> testEmail(
            @RequestParam String to,
            @RequestParam String firstName,
            @RequestParam String temporaryPassword) {
        emailService.sendAccountCreatedEmail(to, firstName, temporaryPassword);
        return ResponseEntity.ok("Email sent to " + to);
    }
}