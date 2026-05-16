package tn.star.Pfe.event;

import tn.star.Pfe.entity.election.Election;
import tn.star.Pfe.entity.election.ElectionCall;

public record ElectionPublishedEvent(Object source, Election election, ElectionCall call, Long userId) {}
