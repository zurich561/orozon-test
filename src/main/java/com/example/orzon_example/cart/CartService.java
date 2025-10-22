package com.example.orzon_example.cart;

import com.example.orzon_example.event.EventService;
import com.example.orzon_example.event.UserEventType;
import com.example.orzon_example.product.Product;
import com.example.orzon_example.product.ProductRepository;
import com.example.orzon_example.user.AppUser;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final EventService eventService;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       ProductRepository productRepository, CouponRepository couponRepository,
                       EventService eventService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        this.eventService = eventService;
    }

    public CartSummary getCart(AppUser user) {
        Cart cart = getOrCreateCart(user);
        return toSummary(cart);
    }

    public CartSummary addItem(AppUser user, @Valid CartModification modification) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(modification.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);
        if (item == null) {
            item = new CartItem(product, modification.quantity(), product.getPrice());
            cart.addItem(item);
        } else {
            item.setQuantity(item.getQuantity() + modification.quantity());
        }
        Cart saved = cartRepository.save(cart);
        eventService.recordEvent(UserEventType.ADD_TO_CART, user, product, "quantity=" + modification.quantity());
        return toSummary(saved);
    }

    public CartSummary updateItemQuantity(AppUser user, Long itemId, int quantity) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cart.getItems().stream()
                .filter(existing -> existing.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        item.setQuantity(quantity);
        Cart saved = cartRepository.save(cart);
        eventService.recordEvent(UserEventType.UPDATE_CART, user, item.getProduct(), "quantity=" + quantity);
        return toSummary(saved);
    }

    public CartSummary removeItem(AppUser user, Long itemId) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cart.getItems().stream()
                .filter(existing -> existing.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        cart.removeItem(item);
        cartItemRepository.delete(item);
        eventService.recordEvent(UserEventType.REMOVE_FROM_CART, user, item.getProduct(), null);
        return toSummary(cartRepository.save(cart));
    }

    public CartSummary applyCoupon(AppUser user, String code) {
        Cart cart = getOrCreateCart(user);
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        cart.setAppliedCoupon(coupon.getCode());
        Cart saved = cartRepository.save(cart);
        eventService.recordEvent(UserEventType.UPDATE_CART, user, null, "coupon=" + code);
        return toSummary(saved);
    }

    public Cart getOrCreateCart(AppUser user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = new Cart(user);
            return cartRepository.save(cart);
        });
    }

    public List<CartItem> getCartItems(AppUser user) {
        return getOrCreateCart(user).getItems();
    }

    public void clearCart(AppUser user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cart.setAppliedCoupon(null);
        cartRepository.save(cart);
    }

    private CartSummary toSummary(Cart cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Coupon coupon = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (cart.getAppliedCoupon() != null) {
            coupon = couponRepository.findByCodeIgnoreCase(cart.getAppliedCoupon()).orElse(null);
            if (coupon != null) {
                discount = subtotal.multiply(coupon.getDiscountPercentage().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            }
        }
        BigDecimal total = subtotal.subtract(discount).max(BigDecimal.ZERO);
        List<CartLine> lines = cart.getItems().stream()
                .map(item -> new CartLine(item.getId(), item.getProduct().getId(), item.getProduct().getTitle(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal()))
                .toList();
        return new CartSummary(lines, subtotal, discount, total, coupon != null ? coupon.getCode() : null);
    }

    public record CartModification(@NotNull Long productId, @Min(1) int quantity) {
    }

    public record CartLine(Long itemId, Long productId, String title, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    }

    public record CartSummary(List<CartLine> items, BigDecimal subtotal, BigDecimal discount, BigDecimal total, String couponCode) {
    }
}
