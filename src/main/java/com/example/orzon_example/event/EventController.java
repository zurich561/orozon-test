package com.example.orzon_example.event;

import com.example.orzon_example.product.Product;
import com.example.orzon_example.product.ProductService;
import com.example.orzon_example.user.AppUser;
import com.example.orzon_example.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final ProductService productService;

    public EventController(EventService eventService, UserService userService, ProductService productService) {
        this.eventService = eventService;
        this.userService = userService;
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Void> recordEvent(@Valid @RequestBody EventRequest request,
                                            @AuthenticationPrincipal UserDetails principal) {
        AppUser user = null;
        if (principal != null) {
            user = userService.findByUsername(principal.getUsername()).orElse(null);
        }
        Product product = null;
        if (request.productId() != null) {
            product = productService.findById(request.productId()).orElse(null);
        }
        eventService.recordEvent(request.type(), user, product, request.details());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/metrics")
    public EventService.DashboardSummary metrics() {
        return eventService.getDashboardSummary();
    }

    public record EventRequest(@NotNull UserEventType type,
                               Long productId,
                               String details) {
    }
}
