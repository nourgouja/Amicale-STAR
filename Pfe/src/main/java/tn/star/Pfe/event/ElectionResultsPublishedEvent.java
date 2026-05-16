package tn.star.Pfe.event;

import tn.star.Pfe.entity.election.Election;

public record ElectionResultsPublishedEvent(Object source, Election election, Long userId, String resultsJson) {}
