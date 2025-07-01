package tn.enicarthage.Festiv.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, String> otpStore = new HashMap<>();

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email manquant.");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Code de vérification");
        message.setText("Votre code de vérification est : " + otp);

        try {
            mailSender.send(message);
            otpStore.put(email, otp);
            return ResponseEntity.ok("OTP envoyé à : " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String code = payload.get("otp");

        if (email == null || code == null) {
            return ResponseEntity.badRequest().body("Email ou code manquant.");
        }

        String expectedOtp = otpStore.get(email);
        if (code.equals(expectedOtp)) {
            otpStore.remove(email);
            return ResponseEntity.ok("OTP valide !");
        } else {
            return ResponseEntity.status(403).body("OTP invalide.");
        }
    }
}