📄 RAPPORT D'AVANCEMENT TECHNIQUE : PROJET TECHSTORE
Statut : Phase 1 - Initialisation & Conception (Terminée)
Environnement : VS Code, Java 17, Spring Boot 3, Base de données locale XAMPP

1. CONTEXTE ET TECHNIQUE D'EMPILAGE
Projet : TechStore (E-commerce électronique pour le Cameroun/Afrique Centrale).
Frontend prévu : React JS + Vite + TailwindCSS (Non commencé).
Backend en cours : Spring Boot 3 (Java 17), Architecture MVC stricte par couches (pas d'architecture par modules/features).
Base de données : Gérée via XAMPP. ATTENTION TECHNIQUE MAJEURE : Bien que le bouton XAMPP dise "MySQL", c'est en réalité MariaDB (démontré par une erreur sur les index JSON). Le dialecte configuré dans Spring est donc .
Fichier de conf : est configuré (port 8080, connexion root sans mot de passe sur , DDL en mode ).errno 150org.hibernate.dialect.MariaDBDialect application.propertiestechstore_dbvalidate

2. ÉTAT DE LA BASE DE DONNEES (100% TERMINÉ)
La base a été entièrement créée et optimisée. Voici les décisions techniques spécifiques qui ont été appliquées :techstore_db

Gestion des Variantes (Produits) : Au lieu de colonnes statiques (couleur, ram, stockage), les variantes utilisent un champ . Exemple : . Les spécifications techniques du produit parent utilisent également un champ .attributes JSON{"Couleur": "Noir", "Stockage": "256Go"}specifications JSON
Sécurité B2B (Prix d'achat) : Des colonnes ont été ajoutées aux tables et pour calculer la marge. Elles ne doivent JAMAIS être exposées dans les DTO de réponse (utilisation de ).cost_priceproductsproduct_variants@JsonIgnore
Module Fournisseurs (B2B) : Ajouté , et . Les livraisons partielles sont gérées via un champ .supplierspurchase_orderspurchase_order_itemsquantity_received
Snapshot des commandes : La table contient des champs et pour figer l'historique même si le produit est modifié/supprimé plus tard.order_itemsproduct_namevariant_attributes (JSON)
Avis avancé : La table a été modifiée pour inclure des notes par critère ( , ), des compteurs de votes utiles ( ), et un champ de réponse admin ( ).reviewsrating_qualityrating_deliveryuseful_countadmin_reply
Soft Delete : Ajout de sur la table .is_deletedusers
Procédures Stockées (Déjà créées dans MariaDB)
La logique de stock n'est pas gérée par du code Java métier, mais par la base pour éviter les "Race Conditions" (survente) :

deduct_stock(p_product_id, p_variant_id, p_quantity, OUT p_success, OUT p_message): Utilisez un . Appelée par le webhook de paiement.SELECT ... FOR UPDATE
add_stock(...): Pour les réceptions de bons de commande fournisseur.
update_product_rating(p_product_id): Recalculez la moyenne des avis après chaque nouvel avis.
3. ARCHITECTURE DU BACKEND SPRING BOOT (STRUCTURE MVC)
La structure des dossiers a été générée dans . C'est une architecture MVC en couches strictes :src/main/java/com/techstore/techstoreapi/

model/: Entités JPA (Mapping direct de la BD). Seuls (dans dto) et , , ont du code pour l'instant.ApiResponse.javaCategory.javaProduct.javaProductVariant.java
dto/request/: Classes recevant les données du Frontend (ex: ).LoginRequest
dto/response/: Classes formatant les données pour le Frontend (ex : sans le ). Contenu le standardisé .ProductResponsecost_priceApiResponse{status, code, message, timestamp, data}
controller/: Points de terminaison REST. (Sous-dossier pour le back-office).admin/
repository/: Interfaces .JpaRepository
service/+ : Logique métier (Interfaces puis Implémentations).service/impl/
config/: Fichiers vides prêts pour , .SecurityConfigCorsConfig
exception/: Contient et (codes).GlobalExceptionHandlerResourceNotFoundException
4. CE QUI A ÉTÉ CODÉ JUSQU'ICI
SQL : La totalité du script de création de la BDD avec les contraintes, index et procédures fournis.
Java - Modèle : Category.java (entité de base), (avec gestion JSON, sur cost_price, liaison vers Category et Supplier), (avec gestion JSON des attributs).Product.java@JsonIgnoreProductVariant.java
Java - Infrastructure : ApiResponse.java (gestion des réponses standardisées), et .GlobalExceptionHandler.javaResourceNotFoundException.java
Fichiers vides : Tous les autres fichiers et dossiers de l'arborescence MVC ont été créés (via le terminal de commande) mais sont vides.
5. PROCHAINE ÉTAPE À RÉALISER (Pour l'IA suivante)
La prochaine étape consiste à peupler le package et pour le module le plus simple afin de tester le cycle complet MVC, puis de configurer Spring Security.repository/service/

Consignes pour la suite :

Terminer les entités restantes en se basant strictement sur le schéma SQL fourni précédemment (en utilisant pour les champs JSON, car c'est du MariaDB).model/@JdbcTypeCode(SqlTypes.JSON)
Créer les de base ( , , etc.).RepositoryUserRepositoryProductRepository
Mettre en place temporairement en mode pour ne pas bloquer les tests Postman.SecurityConfigpermitAll()
Ne pas toucher au frontend React pour le moment.
Ne pas implémenter le paiement CinetPay ni le Chatbot Rasa tout de suite (priorité au CRUD Catalog et Auth basique).