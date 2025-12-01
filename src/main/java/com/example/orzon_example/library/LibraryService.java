package com.example.orzon_example.library;

import com.example.orzon_example.event.EventService;
import com.example.orzon_example.event.UserEventType;
import com.example.orzon_example.product.Product;
import com.example.orzon_example.product.ProductRepository;
import com.example.orzon_example.user.AppUser;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional
public class LibraryService {

    private final PurchasedBookRepository repository;
    private final ProductRepository productRepository;
    private final EventService eventService;

    public LibraryService(PurchasedBookRepository repository, ProductRepository productRepository, EventService eventService) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.eventService = eventService;
    }

    public List<BookView> getLibrary(AppUser user) {
        return repository.findByUserId(user.getId()).stream()
                .map(book -> new BookView(book.getProduct().getId(), book.getProduct().getTitle(), book.getProduct().getAuthor(), book.getPurchasedAt()))
                .toList();
    }

    public String readBook(AppUser user, Long productId) {
        PurchasedBook purchased = getPurchasedBook(user, productId);
        eventService.recordEvent(UserEventType.READ, user, purchased.getProduct(), null);
        return purchased.getContentSnapshot();
    }

    public byte[] downloadBook(AppUser user, Long productId) {
        PurchasedBook purchased = getPurchasedBook(user, productId);
        eventService.recordEvent(UserEventType.DOWNLOAD, user, purchased.getProduct(), null);
        return purchased.getContentSnapshot().getBytes(StandardCharsets.UTF_8);
    }

    public void registerPurchase(AppUser user, List<Product> products) {
        products.forEach(product -> {
            if (!repository.existsByUserIdAndProductId(user.getId(), product.getId())) {
                repository.save(new PurchasedBook(user, product, product.getDigitalContent()));
            }
        });
        eventService.recordEvent(UserEventType.PURCHASE, user, null, "items=" + products.size());
    }

    private PurchasedBook getPurchasedBook(AppUser user, Long productId) {
        return repository.findByUserId(user.getId()).stream()
                .filter(book -> book.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Book not in library"));
    }

    public record BookView(Long productId, String title, String author, java.time.LocalDateTime purchasedAt) {
    }
}
