package tn.star.Pfe.service.echeance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EcheanceScheduler {

    private final EcheanceService echeanceService;

    @Scheduled(cron = "${app.scheduler.reminders.cron:0 0 6 * * *}")
    public void envoyerRappels() {
        log.info("Scheduler: envoi des rappels d'échéances...");
        echeanceService.envoyerRappels();
    }

    @Scheduled(cron = "${app.scheduler.overdue-check.cron:0 0 1 * * *}")
    public void verifierEcheancesEnRetard() {
        log.info("Scheduler: vérification des échéances en retard...");
        echeanceService.marquerEnRetard();
    }
}
