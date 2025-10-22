package com.example.orzon_example;

import com.example.orzon_example.cart.CartService;
import com.example.orzon_example.cart.CartService.CartItemRequest;
import com.example.orzon_example.cart.CartService.CartRequest;
import com.example.orzon_example.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void summarizeAppliesCouponWhenValidCode() {
        var product = productRepository.findAll().get(0);
        var request = new CartRequest(
                List.of(new CartItemRequest(product.getId(), 2)),
                "WELCOME10"
        );

        var summary = cartService.summarize(request);

        assertThat(summary.items()).hasSize(1);
        assertThat(summary.items().get(0).productId()).isEqualTo(product.getId());
        assertThat(summary.discount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(summary.couponCode()).isEqualTo("WELCOME10");
        assertThat(summary.total()).isEqualTo(summary.subtotal().subtract(summary.discount()));
    }
}
