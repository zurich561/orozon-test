package com.example.orzon_example.address;

import com.example.orzon_example.user.AppUser;
import com.example.orzon_example.user.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;

    public AddressController(AddressService addressService, UserService userService) {
        this.addressService = addressService;
        this.userService = userService;
    }

    @GetMapping
    public List<Address.AddressDto> list(@AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        return addressService.findAddressesForUser(user.getId()).stream().map(Address::toDto).toList();
    }

    @PostMapping
    public Address.AddressDto create(@Valid @RequestBody Address.AddressDto dto,
                                     @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        Address saved = addressService.saveAddress(user.getId(), dto);
        return saved.toDto();
    }

    private AppUser requireUser(UserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Authentication required");
        }
        return userService.findByUsername(principal.getUsername()).orElseThrow();
    }
}
