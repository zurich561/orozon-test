package com.example.orzon_example.web;

import com.example.orzon_example.cart.CartService;
import com.example.orzon_example.user.AppUser;
import com.example.orzon_example.user.UserService;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Validated
public class CartPageController {

    private final CartService cartService;
    private final UserService userService;

    public CartPageController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping("/cart")
    public String cart(Model model, @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        model.addAttribute("cart", cartService.getCart(user));
        return "cart";
    }

    @PostMapping("/cart/apply-coupon")
    public String applyCoupon(@RequestParam("code") String code,
                              @AuthenticationPrincipal UserDetails principal,
                              org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        AppUser user = requireUser(principal);
        try {
            cartService.applyCoupon(user, code);
            redirectAttributes.addFlashAttribute("message", "Gutschein angewendet.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateItem(@RequestParam("itemId") Long itemId,
                             @RequestParam("quantity") @Min(1) int quantity,
                             @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        cartService.updateItemQuantity(user, itemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeItem(@RequestParam("itemId") Long itemId,
                             @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        cartService.removeItem(user, itemId);
        return "redirect:/cart";
    }

    private AppUser requireUser(UserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Bitte melden Sie sich an.");
        }
        return userService.findByUsername(principal.getUsername()).orElseThrow();
    }
}
