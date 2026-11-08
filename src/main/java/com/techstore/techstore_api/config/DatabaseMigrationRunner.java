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

        // 4. S'assurer que sort_order, created_at, updated_at existent dans product_images
        try {
            jdbcTemplate.execute("ALTER TABLE product_images ADD COLUMN sort_order INT DEFAULT 0");
            log.info("✅ [MIGRATION] Colonne product_images.sort_order ajoutée");
        } catch (Exception e) {
            log.info("ℹ️ [MIGRATION] Colonne product_images.sort_order déjà existante");
        }

        try {
            jdbcTemplate.execute("ALTER TABLE product_images ADD COLUMN created_at DATETIME NULL");
            jdbcTemplate.execute("ALTER TABLE product_images ADD COLUMN updated_at DATETIME NULL");
            log.info("✅ [MIGRATION] Colonnes product_images.created_at/updated_at ajoutées");
        } catch (Exception e) {
            log.info("ℹ️ [MIGRATION] Colonnes product_images.created_at/updated_at déjà existantes");
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

        // 12. Agrandir la taille de products.name (évite Data too long for column 'name')
        try {
            jdbcTemplate.execute("ALTER TABLE products MODIFY name VARCHAR(500) NOT NULL");
            log.info("✅ [MIGRATION] products.name agrandi à VARCHAR(500)");
        } catch (Exception e) {
            log.warn("⚠️ [MIGRATION] Erreur agrandissement products.name : {}", e.getMessage());
        }

        // 13. Agrandir la taille de products.slug
        try {
            jdbcTemplate.execute("ALTER TABLE products MODIFY slug VARCHAR(500) NOT NULL");
            log.info("✅ [MIGRATION] products.slug agrandi à VARCHAR(500)");
        } catch (Exception e) {
            log.warn("⚠️ [MIGRATION] Erreur agrandissement products.slug : {}", e.getMessage());
        }

        // 14. Nettoyer les dates '0000-00-00 00:00:00' (évite MysqlDataTruncation)
        try {
            jdbcTemplate.execute("SET SQL_MODE = 'ALLOW_INVALID_DATES'");
            jdbcTemplate.execute("UPDATE products SET created_at = NOW() WHERE created_at = '0000-00-00 00:00:00' OR created_at IS NULL");
            jdbcTemplate.execute("UPDATE products SET updated_at = NOW() WHERE updated_at = '0000-00-00 00:00:00' OR updated_at IS NULL");
            jdbcTemplate.execute("ALTER TABLE products MODIFY created_at DATETIME NULL");
            jdbcTemplate.execute("ALTER TABLE products MODIFY updated_at DATETIME NULL");
            log.info("✅ [MIGRATION] products dates nettoyées et rendues nullables");
        } catch (Exception e) {
            log.warn("⚠️ [MIGRATION] Nettoyage dates products info : {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE product_variants MODIFY created_at DATETIME NULL");
            jdbcTemplate.execute("ALTER TABLE product_variants MODIFY updated_at DATETIME NULL");
            log.info("✅ [MIGRATION] product_variants dates rendues nullables");
        } catch (Exception e) {
            log.info("ℹ️ [MIGRATION] product_variants dates info : {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE carts MODIFY created_at DATETIME NULL");
            jdbcTemplate.execute("ALTER TABLE carts MODIFY updated_at DATETIME NULL");
            log.info("✅ [MIGRATION] carts dates rendues nullables");
        } catch (Exception e) {
            log.info("ℹ️ [MIGRATION] carts dates info : {}", e.getMessage());
        }

        log.info("✅ [MIGRATION] Vérification du schéma terminée.");
    }
}
