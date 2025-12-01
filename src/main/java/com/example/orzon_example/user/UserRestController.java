package com.example.orzon_example.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserProfile me(@AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        return new UserProfile(user.getId(), user.getUsername(), user.getEmail());
    }

    @PutMapping("/me")
    public UserProfile update(@Valid @RequestBody UserService.UpdateUserRequest request,
                              @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        AppUser updated = userService.updateProfile(user.getId(), request);
        return new UserProfile(updated.getId(), updated.getUsername(), updated.getEmail());
    }

    @DeleteMapping("/me")
    public void delete(@AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        userService.deleteAccount(user.getId());
    }

    @GetMapping("/me/export")
    public UserService.UserExport export(@AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        return userService.exportUserData(user);
    }

    private AppUser requireUser(UserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Authentication required");
        }
        return userService.findByUsername(principal.getUsername()).orElseThrow();
    }

    public record UserProfile(Long id, String username, String email) {
    }
}
