package com.techstore.techstore_api.service;

import com.techstore.techstore_api.dto.request.AddressRequest;
import com.techstore.techstore_api.dto.response.AddressResponse;
import java.util.List;

public interface AddressService {
    AddressResponse addAddress(AddressRequest request, String userEmail);
    List<AddressResponse> getMyAddresses(String userEmail);
    AddressResponse updateAddress(Long id, AddressRequest request, String userEmail);
    void deleteAddress(Long id, String userEmail);
}