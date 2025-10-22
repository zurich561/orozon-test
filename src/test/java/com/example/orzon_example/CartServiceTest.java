package com.example.orzon_example;

import com.example.orzon_example.cart.CartService;
import com.example.orzon_example.cart.CartService.CartModification;
import com.example.orzon_example.product.ProductRepository;
import com.example.orzon_example.user.AppUser;
import com.example.orzon_example.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AppUserRepository userRepository;

    private AppUser user;

    @BeforeEach
    void setup() {
        user = userRepository.findByUsernameIgnoreCase("alice").orElseThrow();
        cartService.clearCart(user);
    }

    @Test
    void addItemAndApplyCoupon() {
        var product = productRepository.findAll().get(0);
        cartService.addItem(user, new CartModification(product.getId(), 2));
        var summary = cartService.applyCoupon(user, "WELCOME10");
        assertThat(summary.items()).hasSize(1);
        assertThat(summary.discount()).isGreaterThan(summary.subtotal().multiply(new java.math.BigDecimal("0.05")));
    }
}
