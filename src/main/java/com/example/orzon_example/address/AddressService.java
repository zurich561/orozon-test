package com.example.orzon_example.address;

import com.example.orzon_example.user.AppUser;
import com.example.orzon_example.user.AppUserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Transactional
public class AddressService {

    private static final Map<String, Pattern> POSTAL_CODE_PATTERNS = Map.of(
            "DE", Pattern.compile("^[0-9]{5}$"),
            "AT", Pattern.compile("^[0-9]{4}$"),
            "US", Pattern.compile("^[0-9]{5}(?:-[0-9]{4})?$"),
            "GB", Pattern.compile("^[A-Z]{1,2}[0-9][0-9A-Z]? ?[0-9][A-Z]{2}$", Pattern.CASE_INSENSITIVE)
    );

    private final AddressRepository addressRepository;
    private final AppUserRepository userRepository;

    public AddressService(AddressRepository addressRepository, AppUserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public Address saveAddress(Long userId, @Valid Address.AddressDto dto) {
        AppUser user = userRepository.findById(userId).orElseThrow();
        validatePostalCode(dto.country(), dto.postalCode());
        Address address = new Address(user, dto.type(), dto.street(), dto.city(), dto.postalCode(), dto.country(), dto.state());
        return addressRepository.save(address);
    }

    public List<Address> findAddressesForUser(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address getAddressForUser(Long userId, Long addressId) {
        return addressRepository.findById(addressId)
                .filter(address -> address.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
    }

    private void validatePostalCode(String country, String postalCode) {
        if (!StringUtils.hasText(country) || !StringUtils.hasText(postalCode)) {
            throw new IllegalArgumentException("Postal code and country are required");
        }
        String key = country.toUpperCase(Locale.ROOT);
        Pattern pattern = POSTAL_CODE_PATTERNS.get(key);
        if (pattern != null && !pattern.matcher(postalCode).matches()) {
            throw new IllegalArgumentException("Postal code does not match expected format for " + key);
        }
    }
}
