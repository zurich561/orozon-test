package com.example.orzon_example.cart;

import com.example.orzon_example.user.AppUser;
import com.example.orzon_example.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public CartService.CartSummary getCart(@AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        return cartService.getCart(user);
    }

    @PostMapping("/items")
    public CartService.CartSummary addItem(@Valid @RequestBody CartService.CartModification modification,
                                           @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        return cartService.addItem(user, modification);
    }

    @PutMapping("/items/{itemId}")
    public CartService.CartSummary updateItem(@PathVariable Long itemId,
                                              @Valid @RequestBody UpdateRequest request,
                                              @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        return cartService.updateItemQuantity(user, itemId, request.quantity());
    }

    @DeleteMapping("/items/{itemId}")
    public CartService.CartSummary deleteItem(@PathVariable Long itemId,
                                              @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        return cartService.removeItem(user, itemId);
    }

    @PostMapping("/coupon")
    public CartService.CartSummary applyCoupon(@RequestParam("code") String code,
                                               @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        return cartService.applyCoupon(user, code);
    }

    private AppUser requireUser(UserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Authentication required");
        }
        return userService.findByUsername(principal.getUsername()).orElseThrow();
    }

    public record UpdateRequest(@Min(1) int quantity) {
    }
}
