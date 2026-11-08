package com.techstore.techstore_api.dto.request;

import lombok.Data;

@Data
public class AddressRequest {
    private String label;
    private String street;
    private String city;
    private String region;
    private String country;
    private String phone;
    private boolean isDefault;
    private Double latitude;
    private Double longitude;
}