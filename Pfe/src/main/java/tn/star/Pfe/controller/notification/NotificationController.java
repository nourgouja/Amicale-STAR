package tn.star.Pfe.controller.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tn.star.Pfe.dto.notification.NotificationDto;
import tn.star.Pfe.security.UserPrincipal;
import tn.star.Pfe.service.notification.NotificationStore;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationStore store;

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter stream(@AuthenticationPrincipal UserPrincipal principal) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        store.registerEmitter(principal.getId(), emitter);
        return emitter;
    }

    @GetMapping
    public List<NotificationDto> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return store.getQueue(principal.getId());
    }

    @PostMapping("/read")
    public void markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        store.clearAll(principal.getId());
    }
}
