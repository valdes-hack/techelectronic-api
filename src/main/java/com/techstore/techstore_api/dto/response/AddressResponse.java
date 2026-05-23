package com.techstore.techstore_api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {
    private Long id;
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