package com.techstore.techstore_api.dto.request;

import lombok.Data;

@Data
public class AdminPasswordResetRequest {
    private String newPassword;
}