package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.dto.request.AdminPasswordResetRequest;
import com.techstore.techstore_api.dto.request.UserUpdateRequest;
import com.techstore.techstore_api.dto.response.UserResponse;
import com.techstore.techstore_api.model.User;
import com.techstore.techstore_api.repository.UserRepository;
import com.techstore.techstore_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        // On utilise is_verified comme indicateur d'activation
        user.setVerified(!user.isVerified()); 
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetUserPassword(Long id, AdminPasswordResetRequest request) {
        User user = userRepository.findById(id).orElseThrow();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setDeleted(true); // Soft Delete (colonne is_deleted dans ta BD)
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId()).email(user.getEmail())
                .firstName(user.getFirstName()).lastName(user.getLastName())
                .phone(user.getPhone()).role(user.getRole().name())
                .isVerified(user.isVerified()).createdAt(user.getCreatedAt())
                .build();
    }
    @Override
public UserResponse getMyInfo(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    return mapToResponse(user);
}

@Override
@Transactional
public UserResponse updateProfilePicture(String email, String url) {
    User user = userRepository.findByEmail(email).orElseThrow();
    user.setProfilePictureUrl(url);
    return mapToResponse(userRepository.save(user));
}
}