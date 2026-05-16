package tn.star.Pfe.event;

import tn.star.Pfe.entity.election.CandidateApplication;

public record CandidacySubmittedEvent(Object source, CandidateApplication application, Long userId) {}
