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
    }
}
