src/main/java/com/techstore/techstoreapi/
│
├── TechstoreApiApplication.java          <-- (Le point d'entrée Spring Boot)
│
├── model/                                <-- COUCHE "M" (MODÈLE) : Le miroir de ta BDD
│   ├── User.java
│   ├── Address.java
│   ├── Category.java
│   ├── Product.java
│   ├── ProductVariant.java
│   ├── ProductImage.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Payment.java
│   ├── Review.java
│   ├── SavTicket.java
│   ├── Wishlist.java
│   ├── Coupon.java
│   ├── Supplier.java
│   ├── PurchaseOrder.java
│   ├── PurchaseOrderItem.java
│   ├── ChatSession.java
│   ├── ChatMessage.java
│   └── Notification.java
│
├── dto/                                  <-- COUCHE "V" (VUE) : Formatage JSON pour le Frontend
│   ├── request/                          <-- Ce que le Frontend envoie au Backend
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── CartItemRequest.java
│   │   ├── OrderRequest.java
│   │   └── ReviewRequest.java
│   └── response/                         <-- Ce que le Backend renvoie au Frontend
│       ├── ApiResponse.java              <-- (Le format {status, code, message, data})
│       ├── UserResponse.java
│       ├── ProductResponse.java          <-- (SANS le prix d'achat cost_price !)
│       ├── OrderResponse.java
│       └── ReviewResponse.java
│
├── controller/                           <-- COUCHE "C" (CONTRÔLEUR) : Les Endpoints API
│   ├── AuthController.java               <-- (/api/v1/auth/...)
│   ├── ProductController.java            <-- (/api/v1/products/...)
│   ├── CartController.java               <-- (/api/v1/cart/...)
│   ├── OrderController.java              <-- (/api/v1/orders/...)
│   ├── PaymentController.java            <-- (/api/v1/payments/...)
│   ├── ReviewController.java             <-- (/api/v1/reviews/...)
│   ├── AddressController.java            <-- (/api/v1/addresses/...)
│   ├── WishlistController.java           <-- (/api/v1/wishlists/...)
│   ├── SavController.java                <-- (/api/v1/sav/...)
│   ├── ChatController.java               <-- (/api/v1/chat/...)
│   ├── NotificationController.java       <-- (/api/v1/notifications/...)
│   └── admin/                            <-- (Sécurisé par rôle ADMIN)
│       ├── AdminProductController.java   <-- (/api/v1/admin/products/...)
│       ├── AdminOrderController.java
│       ├── AdminSupplierController.java
│       └── AdminDashboardController.java
│
├── repository/                           <-- COUCHE D'ACCÈS AUX DONNÉES (JPA)
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   ├── ProductVariantRepository.java
│   ├── CategoryRepository.java
│   ├── CartRepository.java
│   ├── OrderRepository.java
│   ├── PaymentRepository.java
│   ├── ReviewRepository.java
│   ├── SupplierRepository.java
│   ├── PurchaseOrderRepository.java
│   └── ... (un repository par table)
│
├── service/                              <-- COUCHE LOGIQUE MÉTIER (Les règles du CdC)
│   ├── AuthService.java                  <-- (Interface)
│   ├── impl/                             <-- (Implémentation)
│   │   └── AuthServiceImpl.java
│   ├── ProductService.java
│   ├── impl/
│   │   └── ProductServiceImpl.java
│   ├── OrderService.java
│   ├── impl/
│   │   └── OrderServiceImpl.java
│   └── ... (un service par contrôleur)
│
├── config/                               <-- CONFIGURATION GLOBALE
│   ├── SecurityConfig.java               <-- (Gestion JWT et rôles)
│   ├── CorsConfig.java                   <-- (Autorisation React)
│   └── OpenApiConfig.java                <-- (Swagger / Documentation)
│
└── exception/                            <-- GESTION DES ERREURS
    ├── GlobalExceptionHandler.java
    └── ResourceNotFoundException.java
