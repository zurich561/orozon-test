package com.example.orzon_example.checkout;

import com.example.orzon_example.address.Address;
import com.example.orzon_example.address.AddressService;
import com.example.orzon_example.cart.CartItem;
import com.example.orzon_example.cart.CartService;
import com.example.orzon_example.event.EventService;
import com.example.orzon_example.event.UserEventType;
import com.example.orzon_example.library.LibraryService;
import com.example.orzon_example.product.Product;
import com.example.orzon_example.user.AppUser;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class CheckoutService {

    private final CartService cartService;
    private final AddressService addressService;
    private final LibraryService libraryService;
    private final EventService eventService;

    public CheckoutService(CartService cartService, AddressService addressService, LibraryService libraryService,
                           EventService eventService) {
        this.cartService = cartService;
        this.addressService = addressService;
        this.libraryService = libraryService;
        this.eventService = eventService;
    }

    public CheckoutResponse checkout(AppUser user, @Valid CheckoutRequest request) {
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            cartService.applyCoupon(user, request.couponCode());
        }
        CartService.CartSummary summary = cartService.getCart(user);
        if (summary.items().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        Address shipping = addressService.getAddressForUser(user.getId(), request.shippingAddressId());
        Address billing = addressService.getAddressForUser(user.getId(), request.billingAddressId());
        BigDecimal shippingCost = summary.total().compareTo(new BigDecimal("30")) >= 0 ? BigDecimal.ZERO : new BigDecimal("4.99");
        BigDecimal tax = summary.total().multiply(new BigDecimal("0.07")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = summary.total().add(shippingCost).add(tax);
        if (request.confirmPurchase()) {
            List<Product> products = cartService.getCartItems(user).stream()
                    .map(CartItem::getProduct)
                    .toList();
            libraryService.registerPurchase(user, products);
            cartService.clearCart(user);
            eventService.recordEvent(UserEventType.CHECKOUT, user, null, "completed");
        } else {
            eventService.recordEvent(UserEventType.CHECKOUT, user, null, "preview");
        }
        return new CheckoutResponse(summary, shippingCost, tax, grandTotal,
                new CheckoutResponse.AddressSummary(shipping.getStreet(), shipping.getCity(), shipping.getCountry()),
                new CheckoutResponse.AddressSummary(billing.getStreet(), billing.getCity(), billing.getCountry()));
    }

    public record CheckoutRequest(@NotNull Long shippingAddressId,
                                  @NotNull Long billingAddressId,
                                  boolean confirmPurchase,
                                  String couponCode) {
    }

    public record CheckoutResponse(CartService.CartSummary cart,
                                   BigDecimal shippingCost,
                                   BigDecimal tax,
                                   BigDecimal grandTotal,
                                   AddressSummary shipping,
                                   AddressSummary billing) {
        public record AddressSummary(String street, String city, String country) {
        }
    }
}
