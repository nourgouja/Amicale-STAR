package tn.star.Pfe.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tn.star.Pfe.entity.election.CandidateApplication;
import tn.star.Pfe.entity.user.User;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.event.ApplicationStatusChangedEvent;
import tn.star.Pfe.event.CandidacySubmittedEvent;
import tn.star.Pfe.event.ElectionCallCreatedEvent;
import tn.star.Pfe.event.ElectionPublishedEvent;
import tn.star.Pfe.event.ElectionResultsPublishedEvent;
import tn.star.Pfe.repository.user.UserRepository;
import tn.star.Pfe.service.email.IEmailService;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElectionEventListener {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IEmailService emailService;
    private final UserRepository userRepository;

    @Async
    @EventListener
    public void onElectionCallCreated(ElectionCallCreatedEvent event) {
        log.info("ElectionCall created: id={}", event.call().getId());

        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            emailService.sendElectionCallCreatedToAdmins(
                    admin.getEmail(),
                    event.call().getTitre(),
                    event.call().getDescription()
            );
        }
    }

    @Async
    @EventListener
    public void onCandidacySubmitted(CandidacySubmittedEvent event) {
        CandidateApplication app = event.application();
        User applicant = app.getUser();
        String callTitre = app.getCall().getTitre();
        String position = app.getPosition().getLabel();

        log.info("Candidacy submitted: appId={} by userId={}", app.getId(), applicant.getId());

        emailService.sendCandidacyConfirmationToApplicant(
                applicant.getEmail(),
                applicant.getPrenom(),
                callTitre,
                position
        );

        List<User> admins = userRepository.findByRole(Role.ADMIN);
        String applicantName = applicant.getPrenom() + " " + applicant.getNom();
        for (User admin : admins) {
            emailService.sendNewCandidacyToAdmins(
                    admin.getEmail(),
                    admin.getPrenom(),
                    applicantName,
                    callTitre,
                    position
            );
        }
    }

    @Async
    @EventListener
    public void onApplicationStatusChanged(ApplicationStatusChangedEvent event) {
        CandidateApplication app = event.application();
        User applicant = app.getUser();
        boolean approved = app.getStatus().name().equals("APPROVED");

        log.info("Application {} status changed: {} -> {}", app.getId(), event.oldStatus(), event.newStatus());

        emailService.sendApplicationDecisionToApplicant(
                applicant.getEmail(),
                applicant.getPrenom(),
                app.getCall().getTitre(),
                app.getPosition().getLabel(),
                approved,
                app.getRejectionReason()
        );
    }

    @Async
    @EventListener
    public void onElectionPublished(ElectionPublishedEvent event) {
        log.info("Election published: id={}", event.election().getId());

        String startDate = event.election().getDateDebut().format(DATE_FMT);
        String endDate = event.election().getDateFin().format(DATE_FMT);
        String titre = event.election().getTitre();

        List<User> voters = userRepository.findAll();
        for (User voter : voters) {
            if (voter.isActif()) {
                emailService.sendElectionOpenToVoter(
                        voter.getEmail(),
                        voter.getPrenom(),
                        titre,
                        startDate,
                        endDate
                );
            }
        }
    }

    @Async
    @EventListener
    public void onElectionResultsPublished(ElectionResultsPublishedEvent event) {
        log.info("Election results published: id={}", event.election().getId());

        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            emailService.sendResultsToAdmin(
                    admin.getEmail(),
                    admin.getPrenom(),
                    event.election().getTitre()
            );
        }
    }
}
