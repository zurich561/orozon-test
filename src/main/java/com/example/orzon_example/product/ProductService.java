package com.example.orzon_example.product;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = "productSearch", key = "#query + '-' + (#filter == null ? 'nofilter' : #filter.cacheKey()) + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductDto> searchProducts(String query, ProductFilter filter, Pageable pageable) {
        Specification<Product> spec = Specification.where(ProductSpecifications.textSearch(query))
                .and(ProductSpecifications.byFilter(filter));
        Page<Product> results = repository.findAll(spec, pageable);
        return results.map(ProductDto::fromEntity);
    }

    public Optional<Product> findById(Long id) {
        return repository.findById(id);
    }

    public List<String> autocompleteTitles(String term) {
        return repository.findTop10ByTitleContainingIgnoreCaseOrderByTitleAsc(term).stream()
                .map(Product::getTitle)
                .toList();
    }

    public List<ProductDto> findByCategory(ProductCategory category, Long excludeProductId) {
        Specification<Product> spec = Specification.where((root, query, cb) -> cb.equal(root.get("category"), category));
        if (excludeProductId != null) {
            spec = spec.and((root, query, cb) -> cb.notEqual(root.get("id"), excludeProductId));
        }
        return repository.findAll(spec, PageRequest.of(0, 5)).stream().map(ProductDto::fromEntity).toList();
    }

    public Page<ProductDto> findByAuthor(String author, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%");
        return repository.findAll(spec, pageable).map(ProductDto::fromEntity);
    }

    public record ProductDto(Long id,
                             String title,
                             String author,
                             String isbn,
                             ProductCategory category,
                             BigDecimal price,
                             String language,
                             ProductFormat format,
                             String description,
                             String coverImageUrl) {
        public static ProductDto fromEntity(Product product) {
            return new ProductDto(product.getId(), product.getTitle(), product.getAuthor(), product.getIsbn(),
                    product.getCategory(), product.getPrice(), product.getLanguage(), product.getFormat(),
                    product.getDescription(), product.getCoverImageUrl());
        }
    }
}
