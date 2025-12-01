package com.example.orzon_example.product;

import java.math.BigDecimal;

public record ProductFilter(ProductCategory category,
                            String author,
                            BigDecimal minPrice,
                            BigDecimal maxPrice,
                            String language,
                            ProductFormat format) {

    public String cacheKey() {
        return String.join(":",
                category == null ? "all" : category.name(),
                author == null ? "all" : author,
                minPrice == null ? "-" : minPrice.toString(),
                maxPrice == null ? "-" : maxPrice.toString(),
                language == null ? "all" : language,
                format == null ? "all" : format.name());
    }
}
