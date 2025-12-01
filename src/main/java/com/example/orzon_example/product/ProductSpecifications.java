package com.example.orzon_example.product;

import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> textSearch(String query) {
        if (query == null || query.isBlank()) {
            return Specification.where(null);
        }
        String like = "%" + query.trim().toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("author")), like),
                cb.like(cb.lower(root.get("isbn")), like)
        );
    }

    public static Specification<Product> byFilter(ProductFilter filter) {
        Specification<Product> spec = Specification.where(null);
        if (filter == null) {
            return spec;
        }
        if (filter.category() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), filter.category()));
        }
        if (filter.author() != null && !filter.author().isBlank()) {
            String authorLike = "%" + filter.author().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("author")), authorLike));
        }
        if (filter.minPrice() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
        }
        if (filter.maxPrice() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
        }
        if (filter.language() != null && !filter.language().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("language")), filter.language().toLowerCase()));
        }
        if (filter.format() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("format"), filter.format()));
        }
        return spec;
    }
}
