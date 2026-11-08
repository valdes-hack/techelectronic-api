package com.techstore.techstore_api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * MIGRATION + SEED du système de COLLECTIONS (modèle Shopify-like).
 *
 * Crée :
 *   1. La table `collections`          (les rayons de la boutique)
 *   2. La table `product_collections`  (la table de liaison produits <-> collections)
 *   3. Les 12 collections PEOC par défaut (idempotent : rien n'est écrasé si déjà présent)
 *
 * S'exécute APRES DatabaseMigrationRunner (@Order(1)).
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class CollectionSeedRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("🚀 [COLLECTIONS] Vérification des tables de collections...");

        // ── 1. TABLE COLLECTIONS ─────────────────────────────────────────────
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS collections (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        slug VARCHAR(255) NOT NULL,
                        description TEXT NULL,
                        icon_url VARCHAR(500) NULL,
                        collection_type VARCHAR(50) NULL DEFAULT 'TYPE',
                        is_active TINYINT(1) NOT NULL DEFAULT 1,
                        sort_order INT NOT NULL DEFAULT 0,
                        created_at DATETIME NULL,
                        updated_at DATETIME NULL,
                        CONSTRAINT uk_collections_slug UNIQUE (slug)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """);
            log.info("✅ [COLLECTIONS] Table 'collections' prête");
        } catch (Exception e) {
            log.warn("ℹ️ [COLLECTIONS] Info table collections : {}", e.getMessage());
        }

        // ── 2. TABLE DE LIAISON PRODUIT <-> COLLECTION ───────────────────────
        // Stratégie défensive (même schéma que le reste du projet) : la table est créée
        // sans contraintes FK, puis les FK sont ajoutées dans des blocs séparés
        // non bloquants (l'intégrité reste aussi garantie par CollectionServiceImpl).
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS product_collections (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        collection_id BIGINT NOT NULL,
                        product_id BIGINT NOT NULL,
                        sort_order INT NOT NULL DEFAULT 0,
                        created_at DATETIME NULL,
                        CONSTRAINT uk_collection_product UNIQUE (collection_id, product_id),
                        INDEX idx_pc_collection (collection_id),
                        INDEX idx_pc_product (product_id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """);
            log.info("✅ [COLLECTIONS] Table de liaison 'product_collections' prête");
        } catch (Exception e) {
            log.warn("ℹ️ [COLLECTIONS] Info table liaison : {}", e.getMessage());
        }

        // ── 2bis. AJOUT DES CLÉS ÉTRANGÈRES (best effort, non bloquant) ───────
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE product_collections
                        ADD CONSTRAINT fk_pc_collection FOREIGN KEY (collection_id)
                        REFERENCES collections(id) ON DELETE CASCADE
                    """);
            log.info("✅ [COLLECTIONS] FK vers collections ajoutée");
        } catch (Exception e) {
            log.warn("ℹ️ [COLLECTIONS] FK collections non ajoutée (déjà présente ou type incompatible) : {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE product_collections
                        ADD CONSTRAINT fk_pc_product FOREIGN KEY (product_id)
                        REFERENCES products(id) ON DELETE CASCADE
                    """);
            log.info("✅ [COLLECTIONS] FK vers products ajoutée");
        } catch (Exception e) {
            log.warn("ℹ️ [COLLECTIONS] FK products non ajoutée (déjà présente ou type incompatible) : {}", e.getMessage());
        }

        // ── 3. SEED DES 12 COLLECTIONS PEOC ──────────────────────────────────
        // Format : {type, sortOrder, slug, emoji, nom, description}
        Object[][] seed = {
                // ── Par TYPE de produit ──
                {"TYPE", 1, "poussins", "🐣", "Poussins", "Poussins d'un jour et jeunes volailles de qualité pour démarrer votre élevage"},
                {"TYPE", 2, "couveuses", "🥚", "Couveuses", "Couveuses automatiques et incubateurs pour une éclosion réussie"},
                {"TYPE", 3, "aliments", "🌾", "Aliments", "Aliments de qualité pour volailles, porcs et petits ruminants"},
                {"TYPE", 4, "abreuvoirs", "💧", "Abreuvoirs", "Abreuvoirs et équipements d'hydratation pour tous vos animaux"},
                {"TYPE", 5, "mangeoires", "🍽️", "Mangeoires", "Mangeoires robustes et adaptées à chaque type d'élevage"},
                {"TYPE", 6, "materiel-veterinaire", "💉", "Matériel vétérinaire", "Vaccins, vitamines et matériel de soins pour la santé de votre cheptel"},
                // ── Par USAGE ──
                {"USAGE", 10, "demarrage-aviculture", "🚀", "Démarrage en aviculture", "Tout le nécessaire pour lancer votre élevage de volailles du bon pied"},
                {"USAGE", 11, "elevage-porcin", "🐖", "Élevage porcin", "Équipements et aliments spécialisés pour l'élevage de porcs"},
                {"USAGE", 12, "petit-ruminant", "🐐", "Petit ruminant", "Solutions complètes pour chèvres, moutons et autres petits ruminants"},
                // ── Par PROMOTION ──
                {"PROMO", 20, "nouveautes", "✨", "Nouveautés", "Les derniers articles arrivés dans la boutique"},
                {"PROMO", 21, "meilleures-ventes", "🏆", "Meilleures ventes", "Les produits préférés de nos éleveurs"},
                {"PROMO", 22, "promotions", "🏷️", "Promotions", "Les meilleures offres du moment, à ne pas manquer"}
        };

        int created = 0;
        for (Object[] row : seed) {
            try {
                String type = (String) row[0];
                int sortOrder = (Integer) row[1];
                String slug = (String) row[2];
                String emoji = (String) row[3];
                String name = (String) row[4];
                String description = (String) row[5];

                int inserted = jdbcTemplate.update(
                        "INSERT IGNORE INTO collections (name, slug, description, icon_url, collection_type, is_active, sort_order, created_at, updated_at) " +
                                "VALUES (?, ?, ?, ?, ?, 1, ?, NOW(), NOW())",
                        name, slug, description, emoji, type, sortOrder);
                if (inserted > 0) created++;
            } catch (Exception e) {
                log.warn("ℹ️ [COLLECTIONS] Seed '{}' : {}", row[2], e.getMessage());
            }
        }

        if (created > 0) {
            log.info("🌱 [COLLECTIONS] {} collection(s) PEOC créée(s)", created);
        } else {
            log.info("ℹ️ [COLLECTIONS] Les collections par défaut existent déjà");
        }

        log.info("✅ [COLLECTIONS] Initialisation terminée");
    }
}
