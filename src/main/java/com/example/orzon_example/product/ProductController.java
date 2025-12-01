package com.example.orzon_example.product;

import com.example.orzon_example.event.EventService;
import com.example.orzon_example.event.UserEventType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final SearchRateLimiter rateLimiter;
    private final EventService eventService;

    public ProductController(ProductService productService, SearchRateLimiter rateLimiter, EventService eventService) {
        this.productService = productService;
        this.rateLimiter = rateLimiter;
        this.eventService = eventService;
    }

    @GetMapping
    public Page<ProductService.ProductDto> searchProducts(@RequestParam(value = "q", required = false) String query,
                                                          @RequestParam(value = "category", required = false) ProductCategory category,
                                                          @RequestParam(value = "author", required = false) String author,
                                                          @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                                                          @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                                                          @RequestParam(value = "language", required = false) String language,
                                                          @RequestParam(value = "format", required = false) ProductFormat format,
                                                          @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
                                                          @RequestParam(value = "size", defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page, size);
        ProductFilter filter = new ProductFilter(category, author, minPrice, maxPrice, language, format);
        var pageResult = productService.searchProducts(query, filter, pageable);
        eventService.recordEvent(UserEventType.SEARCH, null, null, "q=" + (query == null ? "" : query));
        return pageResult;
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(@RequestParam("term") String term, HttpServletRequest request) {
        rateLimiter.verifyRequestAllowed(request.getRemoteAddr());
        return ResponseEntity.ok(productService.autocompleteTitles(term));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductService.ProductDto> getProduct(@PathVariable Long id) {
        return productService.findById(id)
                .map(product -> {
                    eventService.recordEvent(UserEventType.VIEW, null, product, null);
                    return ProductService.ProductDto.fromEntity(product);
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/filters/options")
    public ResponseEntity<FilterOptions> getFilterOptions() {
        return ResponseEntity.ok(new FilterOptions(ProductCategory.values(), ProductFormat.values()));
    }

    public record FilterOptions(ProductCategory[] categories, ProductFormat[] formats) {
    }
}
