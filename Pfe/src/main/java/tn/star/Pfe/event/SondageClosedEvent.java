package tn.star.Pfe.event;

import tn.star.Pfe.entity.sondage.Sondage;

public record SondageClosedEvent(Object source, Sondage sondage, String closedByUsername) {}
