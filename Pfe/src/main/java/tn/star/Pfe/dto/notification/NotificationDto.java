package tn.star.Pfe.dto.notification;

import java.time.LocalDateTime;

public record NotificationDto (
        String id,
        String type,
        String message,
        String link,
        Severity severity,
        LocalDateTime createdAt) {
    public enum Severity { INFO, SUCCESS, WARNING, ERROR }
}
