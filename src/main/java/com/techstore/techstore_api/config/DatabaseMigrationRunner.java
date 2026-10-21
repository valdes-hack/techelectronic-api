package com.techstore.techstore_api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("🚀 [MIGRATION] Vérification et ajustement du schéma de la base de données...");

        // 1. Rendre category_id nullable dans la table products pour éviter l'erreur 409
        try {
            jdbcTemplate.execute("ALTER TABLE products MODIFY category_id BIGINT NULL");
            log.info("✅ [MIGRATION] products.category_id est désormais NULLABLE");
        } catch (Exception e) {
            log.warn("ℹ️ [MIGRATION] Info modification products.category_id : {}", e.getMessage());
        }

        // 2. Nettoyer les icon_url corrompues (localhost imbriqué avec Cloudinary)
        try {
            int fixed = jdbcTemplate.update(
                "UPDATE categories SET icon_url = SUBSTRING(icon_url, INSTR(icon_url, 'https://res.cloudinary.com')) " +
                "WHERE icon_url LIKE '%https://res.cloudinary.com%'"
            );
            if (fixed > 0) {
                log.info("✅ [MIGRATION] {} icône(s) de catégorie Cloudinary nettoyée(s)", fixed);
            }
        } catch (Exception e) {
            log.warn("ℹ️ [MIGRATION] Info nettoyage Cloudinary icon_url : {}", e.getMessage());
        }

        // 3. Remplacer les icon_url localhost résiduelles par un emoji par défaut
        try {
            int replaced = jdbcTemplate.update(
                "UPDATE categories SET icon_url = '🥚' WHERE icon_url LIKE '%localhost:8080%'"
            );
            if (replaced > 0) {
                log.info("✅ [MIGRATION] {} icône(s) localhost remplacée(s) par défaut", replaced);
            }
        } catch (Exception e) {
            log.warn("ℹ️ [MIGRATION] Info remplacement localhost icon_url : {}", e.getMessage());
        }

        // 4. S'assurer que sort_order existe dans product_images
        try {
            jdbcTemplate.execute("ALTER TABLE product_images ADD COLUMN sort_order INT DEFAULT 0");
            log.info("✅ [MIGRATION] Colonne product_images.sort_order ajoutée");
        } catch (Exception e) {
            log.info("ℹ️ [MIGRATION] Colonne product_images.sort_order déjà existante");
        }

        // 5. S'assurer que specifications existe dans products
        try {
            jdbcTemplate.execute("ALTER TABLE products ADD COLUMN specifications TEXT NULL");
            log.info("✅ [MIGRATION] Colonne products.specifications ajoutée");
        } catch (Exception e) {
            log.info("ℹ️ [MIGRATION] Colonne products.specifications déjà existante");
        }

        // 6. S'assurer que attributes existe dans product_variants (type TEXT pour compatibilité max)
        try {
            jdbcTemplate.execute("ALTER TABLE product_variants ADD COLUMN attributes TEXT NULL");
            log.info("✅ [MIGRATION] Colonne product_variants.attributes ajoutée");
        } catch (Exception e) {
            log.info("ℹ️ [MIGRATION] Colonne product_variants.attributes déjà existante");
        }

        // 7. S'assurer que cost_price existe dans product_variants
        try {
            jdbcTemplate.execute("ALTER TABLE product_variants ADD COLUMN cost_price DECIMAL(19,2) NULL");
            log.info("✅ [MIGRATION] Colonne product_variants.cost_price ajoutée");
        } catch (Exception e) {
            log.info("ℹ️ [MIGRATION] Colonne product_variants.cost_price déjà existante");
        }

        // 8. S'assurer que sku existe dans products (nullable)
        try {
            jdbcTemplate.execute("ALTER TABLE products ADD COLUMN sku VARCHAR(255) NULL");
            log.info("✅ [MIGRATION] Colonne products.sku ajoutée");
        } catch (Exception e) {
            log.info("ℹ️ [MIGRATION] Colonne products.sku déjà existante");
        }

        // 9. S'assurer que cost_price existe dans products
        try {
            jdbcTemplate.execute("ALTER TABLE products ADD COLUMN cost_price DECIMAL(19,2) NULL");
            log.info("✅ [MIGRATION] Colonne products.cost_price ajoutée");
        } catch (Exception e) {
            log.info("ℹ️ [MIGRATION] Colonne products.cost_price déjà existante");
        }

        // 10. Rendre sku_variant nullable dans product_variants (évite erreur si non fourni)
        try {
            jdbcTemplate.execute("ALTER TABLE product_variants MODIFY sku_variant VARCHAR(255) NULL");
            log.info("✅ [MIGRATION] product_variants.sku_variant rendu nullable");
        } catch (Exception e) {
            log.info("ℹ️ [MIGRATION] product_variants.sku_variant info : {}", e.getMessage());
        }

        // 11. Rendre price nullable dans product_variants
        try {
            jdbcTemplate.execute("ALTER TABLE product_variants MODIFY price DECIMAL(19,2) NULL");
            log.info("✅ [MIGRATION] product_variants.price rendu nullable");
        } catch (Exception e) {
            log.info("ℹ️ [MIGRATION] product_variants.price info : {}", e.getMessage());
        }

        log.info("✅ [MIGRATION] Vérification du schéma terminée.");
    }
}
