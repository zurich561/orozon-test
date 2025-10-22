package com.example.orzon_example.cart;

import com.example.orzon_example.product.Product;
import com.example.orzon_example.product.ProductRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CartService {

    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    public CartService(ProductRepository productRepository, CouponRepository couponRepository) {
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
    }

    public CartSummary summarize(@Valid CartRequest request) {
        List<CartItemRequest> requestedItems = request.items() == null ? List.of() : request.items();
        Map<Long, Product> productsById = productRepository.findAllById(
                requestedItems.stream().map(CartItemRequest::productId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Product::getId, product -> product));

        List<CartLine> lines = requestedItems.stream()
                .map(item -> toLine(item, productsById))
                .filter(Objects::nonNull)
                .toList();

        BigDecimal subtotal = lines.stream()
                .map(CartLine::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        String appliedCoupon = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            var couponOpt = couponRepository.findByCodeIgnoreCase(request.couponCode());
            if (couponOpt.isPresent()) {
                var coupon = couponOpt.get();
                appliedCoupon = coupon.getCode();
                discount = subtotal.multiply(
                        coupon.getDiscountPercentage().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            }
        }

        BigDecimal total = subtotal.subtract(discount).max(BigDecimal.ZERO);
        return new CartSummary(lines, subtotal, discount, total, appliedCoupon);
    }

    private CartLine toLine(CartItemRequest item, Map<Long, Product> productsById) {
        Product product = productsById.get(item.productId());
        if (product == null) {
            return null;
        }
        BigDecimal unitPrice = product.getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity()));
        return new CartLine(
                product.getId(),
                product.getTitle(),
                product.getAuthor(),
                unitPrice,
                item.quantity(),
                subtotal,
                product.getCoverImageUrl()
        );
    }

    public record CartItemRequest(@NotNull Long productId, @Min(1) int quantity) {
    }

    public record CartRequest(@Valid List<CartItemRequest> items, String couponCode) {
    }

    public record CartLine(Long productId, String title, String author, BigDecimal unitPrice, int quantity,
                           BigDecimal subtotal, String coverImageUrl) {
    }

    public record CartSummary(List<CartLine> items, BigDecimal subtotal, BigDecimal discount, BigDecimal total,
                              String couponCode) {
    }
}
