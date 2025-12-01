package com.example.orzon_example.web;

import com.example.orzon_example.product.ProductCategory;
import com.example.orzon_example.product.ProductFilter;
import com.example.orzon_example.product.ProductFormat;
import com.example.orzon_example.product.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class SearchController {

    private final ProductService productService;

    public SearchController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"/", "/search"})
    public String search(@RequestParam(value = "q", required = false) String query,
                         @RequestParam(value = "category", required = false) ProductCategory category,
                         @RequestParam(value = "author", required = false) String author,
                         @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                         @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                         @RequestParam(value = "language", required = false) String language,
                         @RequestParam(value = "format", required = false) ProductFormat format,
                         Model model) {
        var filter = new ProductFilter(category, author, minPrice, maxPrice, language, format);
        var results = productService.searchProducts(query, filter, PageRequest.of(0, 20));
        model.addAttribute("query", query);
        model.addAttribute("products", results.getContent());
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("formats", ProductFormat.values());
        model.addAttribute("totalResults", results.getTotalElements());
        return "search";
    }
}
