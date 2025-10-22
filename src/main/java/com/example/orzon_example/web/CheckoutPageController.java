package com.example.orzon_example.web;

import com.example.orzon_example.address.AddressService;
import com.example.orzon_example.cart.CartService;
import com.example.orzon_example.checkout.CheckoutService;
import com.example.orzon_example.user.AppUser;
import com.example.orzon_example.user.UserService;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Validated
public class CheckoutPageController {

    private final CheckoutService checkoutService;
    private final AddressService addressService;
    private final UserService userService;
    private final CartService cartService;

    public CheckoutPageController(CheckoutService checkoutService, AddressService addressService,
                                  UserService userService, CartService cartService) {
        this.checkoutService = checkoutService;
        this.addressService = addressService;
        this.userService = userService;
        this.cartService = cartService;
    }

    @GetMapping("/checkout")
    public String view(Model model, @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        model.addAttribute("cart", cartService.getCart(user));
        model.addAttribute("addresses", addressService.findAddressesForUser(user.getId()).stream().map(com.example.orzon_example.address.Address::toDto).toList());
        model.addAttribute("checkoutRequest", new CheckoutForm());
        return "checkout";
    }

    @PostMapping("/checkout/preview")
    public String preview(@ModelAttribute("checkoutRequest") CheckoutForm form,
                          Model model,
                          @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        var response = checkoutService.checkout(user, form.toRequest(false));
        model.addAttribute("summary", response);
        model.addAttribute("cart", response.cart());
        model.addAttribute("addresses", addressService.findAddressesForUser(user.getId()).stream().map(com.example.orzon_example.address.Address::toDto).toList());
        return "checkout";
    }

    @PostMapping("/checkout/confirm")
    public String confirm(@ModelAttribute("checkoutRequest") CheckoutForm form,
                          Model model,
                          @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        var response = checkoutService.checkout(user, form.toRequest(true));
        model.addAttribute("summary", response);
        model.addAttribute("cart", response.cart());
        model.addAttribute("addresses", addressService.findAddressesForUser(user.getId()).stream().map(com.example.orzon_example.address.Address::toDto).toList());
        model.addAttribute("success", true);
        return "checkout";
    }

    private AppUser requireUser(UserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Bitte anmelden");
        }
        return userService.findByUsername(principal.getUsername()).orElseThrow();
    }

    public static class CheckoutForm {
        @NotNull
        private Long shippingAddressId;
        @NotNull
        private Long billingAddressId;
        private String couponCode;

        public Long getShippingAddressId() {
            return shippingAddressId;
        }

        public void setShippingAddressId(Long shippingAddressId) {
            this.shippingAddressId = shippingAddressId;
        }

        public Long getBillingAddressId() {
            return billingAddressId;
        }

        public void setBillingAddressId(Long billingAddressId) {
            this.billingAddressId = billingAddressId;
        }

        public String getCouponCode() {
            return couponCode;
        }

        public void setCouponCode(String couponCode) {
            this.couponCode = couponCode;
        }

        CheckoutService.CheckoutRequest toRequest(boolean confirm) {
            return new CheckoutService.CheckoutRequest(shippingAddressId, billingAddressId, confirm, couponCode);
        }
    }
}
