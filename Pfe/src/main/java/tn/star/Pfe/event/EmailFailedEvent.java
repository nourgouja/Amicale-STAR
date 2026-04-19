package tn.star.Pfe.event;

public record EmailFailedEvent(String recipientEmail, String reason) {}
