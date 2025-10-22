package com.example.orzon_example.library;

import com.example.orzon_example.user.AppUser;
import com.example.orzon_example.user.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService libraryService;
    private final UserService userService;

    public LibraryController(LibraryService libraryService, UserService userService) {
        this.libraryService = libraryService;
        this.userService = userService;
    }

    @GetMapping
    public List<LibraryService.BookView> getLibrary(@AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        return libraryService.getLibrary(user);
    }

    @GetMapping("/{productId}/read")
    public ResponseEntity<String> readBook(@PathVariable Long productId,
                                           @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        return ResponseEntity.ok(libraryService.readBook(user, productId));
    }

    @GetMapping("/{productId}/download")
    public ResponseEntity<byte[]> downloadBook(@PathVariable Long productId,
                                               @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        byte[] content = libraryService.downloadBook(user, productId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=book-" + productId + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }

    private AppUser requireUser(UserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Authentication required");
        }
        return userService.findByUsername(principal.getUsername()).orElseThrow();
    }
}
