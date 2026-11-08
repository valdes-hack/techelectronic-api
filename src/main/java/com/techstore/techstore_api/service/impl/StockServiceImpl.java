package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.dto.request.StockRequest;
import com.techstore.techstore_api.model.*;
import com.techstore.techstore_api.repository.*;
import com.techstore.techstore_api.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // ✨ Génère le constructeur pour les "private final"
public class StockServiceImpl implements StockService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductVariantRepository variantRepository;

    @Override
    @Transactional
    public void supplyProduct(StockRequest request) {
        // 1. Récupérer le produit
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        // 2. Mettre à jour le prix d'achat (cost_price) du produit pour les stats
        product.setCostPrice(request.getPurchasePrice());
        productRepository.save(product);

        // 3. Appeler ta procédure SQL add_stock pour augmenter le stock réel
        productRepository.addStock(
            request.getProductId(), 
            request.getVariantId(), 
            request.getQuantity()
        );

        // 4. Enregistrer le mouvement avec le fournisseur (ID 1 par défaut)
        Long sId = (request.getSupplierId() != null) ? request.getSupplierId() : 1L;
        Supplier supplier = supplierRepository.findById(sId)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));

        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = variantRepository.findById(request.getVariantId()).orElse(null);
        }

        // 5. Sauvegarder l'historique du mouvement
        stockMovementRepository.save(StockMovement.builder()
                .product(product)
                .variant(variant)
                .supplier(supplier)
                .quantity(request.getQuantity())
                .purchasePrice(request.getPurchasePrice())
                .build());
    }
}