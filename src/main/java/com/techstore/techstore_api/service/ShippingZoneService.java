package com.techstore.techstore_api.service;

import com.techstore.techstore_api.model.ShippingZone;
import java.util.List;

public interface ShippingZoneService {
    List<ShippingZone> getAllActiveZones();
}