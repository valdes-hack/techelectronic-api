package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.dto.request.AddressRequest;
import com.techstore.techstore_api.dto.response.AddressResponse;
import com.techstore.techstore_api.model.Address;
import com.techstore.techstore_api.model.User;
import com.techstore.techstore_api.repository.AddressRepository;
import com.techstore.techstore_api.repository.UserRepository;
import com.techstore.techstore_api.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AddressResponse addAddress(AddressRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Si cette nouvelle adresse est par défaut, on désactive l'ancienne
        if (request.isDefault()) {
            handleDefaultAddress(user.getId());
        }

        Address address = Address.builder()
                .user(user).label(request.getLabel()).street(request.getStreet())
                .city(request.getCity()).region(request.getRegion())
                .country(request.getCountry()).phone(request.getPhone())
                .isDefault(request.isDefault()).latitude(request.getLatitude())
                .longitude(request.getLongitude()).build();

        return mapToResponse(addressRepository.save(address));
    }

    @Override
    public List<AddressResponse> getMyAddresses(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return addressRepository.findByUserId(user.getId()).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request, String userEmail) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adresse non trouvée"));

        // Sécurité : Vérifier que l'adresse appartient bien à l'utilisateur connecté
        if (!address.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Accès refusé");
        }

        if (request.isDefault()) {
            handleDefaultAddress(address.getUser().getId());
        }

        address.setLabel(request.getLabel());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setRegion(request.getRegion());
        address.setPhone(request.getPhone());
        address.setDefault(request.isDefault());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());

        return mapToResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(Long id, String userEmail) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adresse non trouvée"));
        
        if (!address.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Accès refusé");
        }
        addressRepository.delete(address);
    }

    // Méthode privée pour gérer l'adresse par défaut unique
    private void handleDefaultAddress(Long userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(a -> {
                    a.setDefault(false);
                    addressRepository.save(a);
                });
    }

    private AddressResponse mapToResponse(Address address) {
    return AddressResponse.builder()
            .id(address.getId())
            .label(address.getLabel()) // <-- Correction ici : ajoute get et les parenthèses
            .street(address.getStreet())
            .city(address.getCity())
            .region(address.getRegion())
            .country(address.getCountry())
            .phone(address.getPhone())
            .isDefault(address.isDefault())
            .latitude(address.getLatitude())
            .longitude(address.getLongitude())
            .build();
}
}