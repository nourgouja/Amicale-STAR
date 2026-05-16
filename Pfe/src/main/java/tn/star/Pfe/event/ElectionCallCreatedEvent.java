package tn.star.Pfe.event;

import tn.star.Pfe.entity.election.ElectionCall;

public record ElectionCallCreatedEvent(Object source, ElectionCall call, Long userId) {}
