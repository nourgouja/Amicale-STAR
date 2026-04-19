// service/notification/NotificationScheduler.java
package tn.star.Pfe.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.star.Pfe.entity.Echeance;
import tn.star.Pfe.enums.StatutPaiement;
import tn.star.Pfe.event.EcheanceOverdueEvent;
import tn.star.Pfe.repository.EcheanceRepository;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final EcheanceRepository echeanceRepository;
    private final ApplicationEventPublisher publisher;

    @Scheduled(cron = "0 0 8 * * *")
    public void checkOverdueEcheances() {
        List<Echeance> overdue = echeanceRepository
                .findByDateEcheanceBeforeAndStatut(LocalDate.now(), StatutPaiement.EN_ATTENTE);
        overdue.forEach(e -> publisher.publishEvent(new EcheanceOverdueEvent(e)));
    }
}
