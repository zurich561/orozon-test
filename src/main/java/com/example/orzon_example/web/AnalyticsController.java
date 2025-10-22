package com.example.orzon_example.web;

import com.example.orzon_example.event.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalyticsController {

    private final EventService eventService;

    public AnalyticsController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        model.addAttribute("dashboard", eventService.getDashboardSummary());
        return "analytics";
    }
}
