package com.example.orzon_example.web;

import com.example.orzon_example.event.EventService;
import com.example.orzon_example.event.UserEventType;
import com.example.orzon_example.product.ProductService;
import com.example.orzon_example.recommendation.RecommendationService;
import com.example.orzon_example.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductDetailController {

    private final ProductService productService;
    private final RecommendationService recommendationService;
    private final EventService eventService;
    private final UserService userService;

    public ProductDetailController(ProductService productService, RecommendationService recommendationService,
                                   EventService eventService, UserService userService) {
        this.productService = productService;
        this.recommendationService = recommendationService;
        this.eventService = eventService;
        this.userService = userService;
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        var product = productService.findById(id).orElseThrow();
        var recommendations = recommendationService.getRecommendations(id);
        model.addAttribute("product", ProductService.ProductDto.fromEntity(product));
        model.addAttribute("similar", recommendations.similarTitles());
        model.addAttribute("alsoBought", recommendations.customersAlsoBought());
        var user = principal != null ? userService.findByUsername(principal.getUsername()).orElse(null) : null;
        eventService.recordEvent(UserEventType.VIEW, user, product, "page");
        return "product-detail";
    }
}
