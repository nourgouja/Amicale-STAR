package tn.star.Pfe.event;

import tn.star.Pfe.entity.election.CandidateApplication;
import tn.star.Pfe.enums.ApprovalStatus;

public record ApplicationStatusChangedEvent(
        Object source,
        CandidateApplication application,
        ApprovalStatus oldStatus,
        ApprovalStatus newStatus,
        Long adminId
) {}
