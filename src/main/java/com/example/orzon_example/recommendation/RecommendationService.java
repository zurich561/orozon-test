package com.example.orzon_example.recommendation;

import com.example.orzon_example.library.PurchasedBook;
import com.example.orzon_example.library.PurchasedBookRepository;
import com.example.orzon_example.product.Product;
import com.example.orzon_example.product.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ProductService productService;
    private final PurchasedBookRepository purchasedBookRepository;

    public RecommendationService(ProductService productService, PurchasedBookRepository purchasedBookRepository) {
        this.productService = productService;
        this.purchasedBookRepository = purchasedBookRepository;
    }

    public RecommendationResult getRecommendations(Long productId) {
        Product product = productService.findById(productId).orElseThrow();
        List<ProductService.ProductDto> similar = productService.findByCategory(product.getCategory(), productId);
        List<ProductService.ProductDto> alsoBought = customersAlsoBought(productId);
        return new RecommendationResult(similar, alsoBought);
    }

    private List<ProductService.ProductDto> customersAlsoBought(Long productId) {
        List<PurchasedBook> allPurchases = purchasedBookRepository.findAll();
        Map<Long, Set<Long>> userToProducts = allPurchases.stream()
                .collect(Collectors.groupingBy(purchase -> purchase.getUser().getId(),
                        Collectors.mapping(purchase -> purchase.getProduct().getId(), Collectors.toSet())));
        Set<Long> candidateProductIds = userToProducts.values().stream()
                .filter(products -> products.contains(productId))
                .flatMap(products -> products.stream().filter(id -> !id.equals(productId)))
                .collect(Collectors.toSet());
        return candidateProductIds.stream()
                .map(id -> productService.findById(id).map(ProductService.ProductDto::fromEntity))
                .flatMap(Optional::stream)
                .limit(5)
                .toList();
    }

    public record RecommendationResult(List<ProductService.ProductDto> similarTitles,
                                       List<ProductService.ProductDto> customersAlsoBought) {
    }
}
