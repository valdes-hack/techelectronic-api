package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.model.Supplier;
import com.techstore.techstore_api.repository.SupplierRepository;
import com.techstore.techstore_api.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
@Override
@Transactional
public Supplier createSupplier(Supplier supplier) {
    // ✨ GÉNÉRATION AUTOMATIQUE DU SLUG ✨
    if (supplier.getSlug() == null || supplier.getSlug().isEmpty()) {
        supplier.setSlug(supplier.getName().toLowerCase()
            .replaceAll("[^a-z0-9]", "-") 
            + "-" + System.currentTimeMillis() % 1000);
    }
    return supplierRepository.save(supplier);
}

   @Override
@Transactional
public Supplier updateSupplier(Long id, Supplier details) {
    Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fournisseur introuvable"));

    supplier.setName(details.getName());
    // On ne change le slug que si le nom a changé
    supplier.setSlug(details.getName().toLowerCase().replaceAll("[^a-z0-9]", "-"));
    supplier.setContactName(details.getContactName());
    supplier.setEmail(details.getEmail());
    supplier.setPhone(details.getPhone());
    supplier.setCity(details.getCity());
    supplier.setCountry(details.getCountry());
    supplier.setActive(details.isActive());

    return supplierRepository.save(supplier);
}


@Override
public void deleteSupplier(Long id) {
    Supplier s = supplierRepository.findById(id).orElseThrow();
    s.setActive(false); // Soft Delete
    supplierRepository.save(s);
}

@Override
public List<Supplier> getAllSuppliers() {
    return supplierRepository.findAll();
}
@Override
@Transactional(readOnly = true)
public Supplier getSupplierById(Long id) {
    return supplierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
}
}