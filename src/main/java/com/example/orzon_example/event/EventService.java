package com.example.orzon_example.event;

import com.example.orzon_example.product.Product;
import com.example.orzon_example.user.AppUser;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    private final UserEventRepository repository;

    public EventService(UserEventRepository repository) {
        this.repository = repository;
    }

    public void recordEvent(UserEventType type, AppUser user, Product product, String details) {
        UserEvent event = new UserEvent(user, product, type, details);
        repository.save(event);
    }

    public EventMetrics getMetrics() {
        List<UserEvent> events = repository.findAll();
        Map<UserEventType, Long> counts = events.stream().collect(Collectors.groupingBy(UserEvent::getEventType, Collectors.counting()));
        long last24h = repository.findByCreatedAtAfter(LocalDateTime.now().minusHours(24)).size();
        Map<LocalDate, Long> daily = events.stream().collect(Collectors.groupingBy(event -> event.getCreatedAt().toLocalDate(), Collectors.counting()));
        long uniqueUsers = events.stream().map(event -> event.getUser() != null ? event.getUser().getId() : null).filter(id -> id != null).distinct().count();
        return new EventMetrics(counts, last24h, daily, uniqueUsers);
    }

    public DashboardSummary getDashboardSummary() {
        EventMetrics metrics = getMetrics();
        long total = metrics.counts().values().stream().mapToLong(Long::longValue).sum();
        long purchases = metrics.counts().getOrDefault(UserEventType.PURCHASE, 0L);
        double conversionRate = total == 0 ? 0 : (double) purchases / total;
        return new DashboardSummary(metrics, conversionRate);
    }

    public record EventMetrics(Map<UserEventType, Long> counts,
                               long eventsLast24Hours,
                               Map<LocalDate, Long> dailyCounts,
                               long uniqueUsers) {
        public EventMetrics {
            counts = counts != null ? counts : new EnumMap<>(UserEventType.class);
            dailyCounts = dailyCounts != null ? dailyCounts : Map.of();
        }
    }

    public record DashboardSummary(EventMetrics metrics, double conversionRate) {
    }
}
