package tn.star.Pfe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.star.Pfe.service.email.EmailService;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private EmailService emailService;
    @PostMapping("/email")
    public ResponseEntity<String> testEmail(@RequestParam String to) {
        emailService.sendWelcomeEmail(to, "Test User");
        return ResponseEntity.ok("Email sent to " + to);
    }
}