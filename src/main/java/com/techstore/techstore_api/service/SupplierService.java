package com.techstore.techstore_api.service;

import com.techstore.techstore_api.model.Supplier;
import java.util.List;

public interface SupplierService {
    Supplier createSupplier(Supplier supplier);
    Supplier updateSupplier(Long id, Supplier details);
        Supplier getSupplierById(Long id);

    void deleteSupplier(Long id);
    List<Supplier> getAllSuppliers();
}