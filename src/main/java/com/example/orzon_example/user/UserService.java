package com.example.orzon_example.user;

import com.example.orzon_example.address.Address;
import com.example.orzon_example.address.AddressService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AddressService addressService;

    public UserService(AppUserRepository repository, PasswordEncoder passwordEncoder, AddressService addressService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.addressService = addressService;
    }

    public AppUser registerUser(@Valid RegisterRequest request) {
        repository.findByUsernameIgnoreCase(request.username())
                .ifPresent(user -> { throw new IllegalArgumentException("Username already taken"); });
        repository.findByEmailIgnoreCase(request.email())
                .ifPresent(user -> { throw new IllegalArgumentException("Email already registered"); });
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        AppUser user = new AppUser(request.username(), request.email(), passwordEncoder.encode(request.password()), roles);
        return repository.save(user);
    }

    public Optional<AppUser> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<AppUser> findByUsername(String username) {
        return repository.findByUsernameIgnoreCase(username);
    }

    public AppUser updateProfile(Long userId, @Valid UpdateUserRequest request) {
        AppUser user = repository.findById(userId).orElseThrow();
        if (!user.getEmail().equalsIgnoreCase(request.email())) {
            repository.findByEmailIgnoreCase(request.email())
                    .ifPresent(existing -> { throw new IllegalArgumentException("Email already registered"); });
        }
        user.setEmail(request.email());
        user.setUsername(request.username());
        return repository.save(user);
    }

    public void deleteAccount(Long userId) {
        repository.deleteById(userId);
    }

    public void resetPassword(@Email String email, @Size(min = 8) String newPassword) {
        AppUser user = repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    public UserExport exportUserData(AppUser user) {
        return new UserExport(user.getUsername(), user.getEmail(),
                addressService.findAddressesForUser(user.getId()).stream().map(Address::toDto).toList());
    }

    public record RegisterRequest(@NotBlank String username,
                                  @NotBlank @Email String email,
                                  @NotBlank @Size(min = 8) String password) {
    }

    public record UpdateUserRequest(@NotBlank String username,
                                    @NotBlank @Email String email) {
    }

    public record UserExport(String username, String email, java.util.List<Address.AddressDto> addresses) {
    }
}
