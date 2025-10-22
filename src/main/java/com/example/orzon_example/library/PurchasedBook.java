package com.example.orzon_example.library;

import com.example.orzon_example.product.Product;
import com.example.orzon_example.user.AppUser;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchased_books")
public class PurchasedBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private LocalDateTime purchasedAt = LocalDateTime.now();

    @Lob
    private String contentSnapshot;

    public PurchasedBook() {
    }

    public PurchasedBook(AppUser user, Product product, String contentSnapshot) {
        this.user = user;
        this.product = product;
        this.contentSnapshot = contentSnapshot;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Product getProduct() {
        return product;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public String getContentSnapshot() {
        return contentSnapshot;
    }
}
