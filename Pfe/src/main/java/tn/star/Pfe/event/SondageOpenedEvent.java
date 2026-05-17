package tn.star.Pfe.event;

import tn.star.Pfe.entity.sondage.Sondage;

public record SondageOpenedEvent(Object source, Sondage sondage) {}
