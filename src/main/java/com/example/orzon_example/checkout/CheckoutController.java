package com.example.orzon_example.checkout;

import com.example.orzon_example.user.AppUser;
import com.example.orzon_example.user.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final UserService userService;

    public CheckoutController(CheckoutService checkoutService, UserService userService) {
        this.checkoutService = checkoutService;
        this.userService = userService;
    }

    @PostMapping
    public CheckoutService.CheckoutResponse checkout(@Valid @RequestBody CheckoutService.CheckoutRequest request,
                                                     @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        return checkoutService.checkout(user, request);
    }

    private AppUser requireUser(UserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Authentication required");
        }
        return userService.findByUsername(principal.getUsername()).orElseThrow();
    }
}
