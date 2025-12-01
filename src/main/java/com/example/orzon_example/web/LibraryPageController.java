package com.example.orzon_example.web;

import com.example.orzon_example.library.LibraryService;
import com.example.orzon_example.user.AppUser;
import com.example.orzon_example.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LibraryPageController {

    private final LibraryService libraryService;
    private final UserService userService;

    public LibraryPageController(LibraryService libraryService, UserService userService) {
        this.libraryService = libraryService;
        this.userService = userService;
    }

    @GetMapping("/library")
    public String library(Model model, @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        model.addAttribute("books", libraryService.getLibrary(user));
        return "library";
    }

    @GetMapping("/library/read/{productId}")
    public String read(@PathVariable Long productId,
                       Model model,
                       @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        model.addAttribute("content", libraryService.readBook(user, productId));
        model.addAttribute("productId", productId);
        return "reader";
    }

    private AppUser requireUser(UserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Bitte anmelden");
        }
        return userService.findByUsername(principal.getUsername()).orElseThrow();
    }
}
