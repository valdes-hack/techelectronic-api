package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.model.ShippingZone;
import com.techstore.techstore_api.repository.ShippingZoneRepository;
import com.techstore.techstore_api.service.ShippingZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingZoneServiceImpl implements ShippingZoneService {

    private final ShippingZoneRepository shippingZoneRepository;

    @Override
    public List<ShippingZone> getAllActiveZones() {
        return shippingZoneRepository.findByIsActiveTrue();
    }
}