package com.example.orzon_example;

import com.example.orzon_example.product.ProductCategory;
import com.example.orzon_example.product.ProductFilter;
import com.example.orzon_example.product.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    void findsProductsByCategoryAndQuery() {
        var filter = new ProductFilter(ProductCategory.TECHNOLOGY, null, null, null, null, null);
        var page = productService.searchProducts("Spring", filter, PageRequest.of(0, 5));
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(page.getContent().get(0).title()).containsIgnoringCase("Spring");
    }

    @Test
    void autocompleteReturnsMatches() {
        var suggestions = productService.autocompleteTitles("Data");
        assertThat(suggestions).anyMatch(title -> title.toLowerCase().contains("data"));
    }
}
