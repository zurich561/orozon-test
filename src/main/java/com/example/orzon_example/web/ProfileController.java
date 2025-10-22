package com.example.orzon_example.web;

import com.example.orzon_example.address.Address;
import com.example.orzon_example.address.AddressService;
import com.example.orzon_example.address.AddressType;
import com.example.orzon_example.user.AppUser;
import com.example.orzon_example.user.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {

    private final UserService userService;
    private final AddressService addressService;

    public ProfileController(UserService userService, AddressService addressService) {
        this.userService = userService;
        this.addressService = addressService;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails principal, Model model) {
        AppUser user = requireUser(principal);
        model.addAttribute("user", user);
        model.addAttribute("addresses", addressService.findAddressesForUser(user.getId()).stream().map(Address::toDto).toList());
        model.addAttribute("updateRequest", new UserService.UpdateUserRequest(user.getUsername(), user.getEmail()));
        model.addAttribute("addressForm", new Address.AddressDto(null, AddressType.SHIPPING, "", "", "", "DE", ""));
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("updateRequest") UserService.UpdateUserRequest request,
                                @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        userService.updateProfile(user.getId(), request);
        return "redirect:/profile";
    }

    @PostMapping("/profile/address")
    public String addAddress(@Valid @ModelAttribute("addressForm") Address.AddressDto dto,
                             @AuthenticationPrincipal UserDetails principal) {
        AppUser user = requireUser(principal);
        addressService.saveAddress(user.getId(), dto);
        return "redirect:/profile";
    }

    private AppUser requireUser(UserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Bitte anmelden");
        }
        return userService.findByUsername(principal.getUsername()).orElseThrow();
    }
}
