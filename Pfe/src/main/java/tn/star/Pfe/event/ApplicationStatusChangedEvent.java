package tn.star.Pfe.event;

import tn.star.Pfe.entity.election.CandidateApplication;
import tn.star.Pfe.enums.StatutDemande;

public record ApplicationStatusChangedEvent(
        Object source,
        CandidateApplication application,
        StatutDemande oldStatus,
        StatutDemande newStatus,
        Long adminId
) {}
