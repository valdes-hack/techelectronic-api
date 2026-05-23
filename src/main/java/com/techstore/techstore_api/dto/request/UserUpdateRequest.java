package com.techstore.techstore_api.dto.request;

import com.techstore.techstore_api.model.Role;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private Role role; // Pour transformer un Client en Admin ou SAV
}