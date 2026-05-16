package tn.star.Pfe.event;

import tn.star.Pfe.entity.election.Election;

public record ExtraRoundCreatedEvent(Object source, Election parentElection, Election extraRound, Long userId) {}
