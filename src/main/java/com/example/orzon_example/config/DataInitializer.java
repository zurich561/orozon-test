package com.example.orzon_example.config;

import com.example.orzon_example.address.Address;
import com.example.orzon_example.address.AddressService;
import com.example.orzon_example.address.AddressType;
import com.example.orzon_example.cart.Coupon;
import com.example.orzon_example.cart.CouponRepository;
import com.example.orzon_example.library.LibraryService;
import com.example.orzon_example.product.Product;
import com.example.orzon_example.product.ProductCategory;
import com.example.orzon_example.product.ProductFormat;
import com.example.orzon_example.product.ProductRepository;
import com.example.orzon_example.user.AppUser;
import com.example.orzon_example.user.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedDatabase(ProductRepository productRepository,
                                   AppUserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   CouponRepository couponRepository,
                                   AddressService addressService,
                                   LibraryService libraryService) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.saveAll(List.of(
                        new Product("Spring Boot Essentials", "Max Mustermann", "ISBN-001", ProductCategory.TECHNOLOGY, new BigDecimal("29.90"), "DE", ProductFormat.EBOOK, "Praxisnaher Einstieg in Spring Boot.", LocalDate.of(2022, 3, 15), null, sampleContent("Spring Boot Essentials")),
                        new Product("Accessible Web Design", "Anna Schmidt", "ISBN-002", ProductCategory.NON_FICTION, new BigDecimal("24.50"), "EN", ProductFormat.PAPERBACK, "Gestaltung barrierefreier Webanwendungen.", LocalDate.of(2021, 8, 10), null, sampleContent("Accessible Web Design")),
                        new Product("Kinder Abenteuergeschichten", "Laura Klein", "ISBN-003", ProductCategory.CHILDREN, new BigDecimal("14.99"), "DE", ProductFormat.HARDCOVER, "Spannende Geschichten für Kinder.", LocalDate.of(2020, 5, 20), null, sampleContent("Abenteuer")),
                        new Product("Data Science Recipes", "John Doe", "ISBN-004", ProductCategory.SCIENCE, new BigDecimal("34.90"), "EN", ProductFormat.EBOOK, "Praktische Rezepte für Data Scientists.", LocalDate.of(2023, 1, 5), null, sampleContent("Data Science")),
                        new Product("Creative Writing", "Sophie Berger", "ISBN-005", ProductCategory.EDUCATION, new BigDecimal("19.90"), "EN", ProductFormat.PAPERBACK, "Werkzeuge für kreative Autor:innen.", LocalDate.of(2019, 11, 30), null, sampleContent("Creative"))
                ));
            }

            if (couponRepository.count() == 0) {
                couponRepository.saveAll(List.of(
                        new Coupon("WELCOME10", new BigDecimal("10")),
                        new Coupon("SPRING15", new BigDecimal("15"))
                ));
            }

            if (userRepository.count() == 0) {
                AppUser user = new AppUser("alice", "alice@example.com", passwordEncoder.encode("password123"), Set.of("USER"));
                userRepository.save(user);
                addressService.saveAddress(user.getId(), new Address.AddressDto(null, AddressType.SHIPPING, "Hauptstr. 1", "Berlin", "10115", "DE", ""));
                addressService.saveAddress(user.getId(), new Address.AddressDto(null, AddressType.BILLING, "Nebenstr. 5", "Berlin", "10117", "DE", ""));
                // give Alice two books
                libraryService.registerPurchase(user, productRepository.findAll().subList(0, 2));
            }
        };
    }

    private String sampleContent(String title) {
        return "Dies ist ein Beispielinhalt für " + title + ". Viel Spaß beim Lesen!";
    }
}
