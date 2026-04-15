package tn.star.Pfe.service.email;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {

    static final String CHARSET = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "@#$%";

    static final int DEFAULT_LENGTH = 12;

    private final int length;
    private final SecureRandom random = new SecureRandom();

    public PasswordGenerator() {
        this(DEFAULT_LENGTH);
    }

    PasswordGenerator(int length) {
        this.length = length;
    }

    public String generate() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }
}