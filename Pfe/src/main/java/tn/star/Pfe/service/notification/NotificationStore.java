// service/notification/NotificationStore.java
package tn.star.Pfe.service.notification;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tn.star.Pfe.dto.notification.NotificationDto;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationStore {

    private static final int MAX_QUEUE_SIZE = 50;

    private final Map<Long, Deque<NotificationDto>> queues = new ConcurrentHashMap<>();
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void push(Long userId, NotificationDto notification) {
        queues.computeIfAbsent(userId, k -> new ArrayDeque<>())
                .addFirst(notification);
        Deque<NotificationDto> q = queues.get(userId);
        while (q.size() > MAX_QUEUE_SIZE) q.removeLast();
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
            } catch (Exception e) {
                emitters.remove(userId);
            }
        }
    }

    public List<NotificationDto> getQueue(Long userId) {
        return new ArrayList<>(queues.getOrDefault(userId, new ArrayDeque<>()));
    }

    public void clearAll(Long userId) {
        queues.remove(userId);
    }

    public void registerEmitter(Long userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
    }
}
