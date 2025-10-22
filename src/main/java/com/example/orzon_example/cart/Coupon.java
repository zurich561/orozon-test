package com.example.orzon_example.cart;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private BigDecimal discountPercentage;

    public Coupon() {
    }

    public Coupon(String code, BigDecimal discountPercentage) {
        this.code = code;
        this.discountPercentage = discountPercentage;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
}
