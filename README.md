# Backend Panier E-commerce – Architecture Hexagonale

## Structure du projet

```
cart-hexa/
├── pom.xml                        * POM parent 
│
├── domain/                        * Cœur métier PUR
│   └── src/main/java/com/ecommerce/cart/domain/
│       ├── model/                 * Entités : Product, Offer, Cart, CartItem, Order
│       ├── port/in/               * Ports d'entrée (interfaces des use cases)
│       ├── port/out/              * Ports de sortie (interfaces des repositories)
│       └── exception/             * Exceptions métier
│
├── application/                   * Implémentation des uses cases
│   └── src/main/java/com/ecommerce/cart/application/
│       └── usecase/               * AddItemToCart, Checkout, GetCart, ...
│
├── infrastructure/                * Adaptateurs sortants (stockage)
│   └── src/main/java/com/ecommerce/cart/infrastructure/
│       └── persistence/           * InMemoryProductRepository, InMemoryCartRepository
│
└── exposition/                    * Adaptateurs entrants (HTTP) + point d'entrée
    └── src/main/java/com/ecommerce/cart/exposition/
        ├── controller/            * CartController, GlobalExceptionHandler
        ├── dto/                   * DTOs de requête/réponse
        ├── mapper/                * Domain → DTO
        └── config/                * SwaggerConfig
```

## Lancer l'application

```bash
# Compiler et lancer depuis la racine
./mvnw spring-boot:run -pl exposition

# Ou compiler tout puis lancer
./mvnw install
java -jar exposition/target/exposition-1.0.0-SNAPSHOT.jar
```

L'application démarre sur `http://localhost:8080`

## Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

## Endpoints REST

| Méthode  | URL                                                    | Description          | HTTP Success |
|----------|--------------------------------------------------------|----------------------|--------------|
| `GET`    | `/api/users/{userId}/cart`                             | Consulter le panier  | 200          |
| `POST`   | `/api/users/{userId}/cart/items`                       | Ajouter un article   | 201          |
| `PUT`    | `/api/users/{userId}/cart/items/{productId}/{offerId}` | Modifier la quantité | 200          |
| `DELETE` | `/api/users/{userId}/cart/items/{productId}/{offerId}` | Supprimer un article | 200          |
| `POST`   | `/api/users/{userId}/checkout`                         | Passer commande      | 201          |

### Produits disponibles (données de test)

| productId             | offerId | état          | prix  | remise | stock       |
|-----------------------|---------|---------------|-------|--------|-------------|
| P001 (iPhone 15 Pro)  | O001    | NEUF          | 1199€ | 0%     | 10          |
| P001                  | O002    | RECONDITIONNE | 1199€ | 15%    | 5           |
| P001                  | O003    | OCCASION      | 1199€ | 30%    | 0 ← rupture |
| P002 (MacBook Air M3) | O004    | NEUF          | 1499€ | 0%     | 8           |
| P002                  | O005    | RECONDITIONNE | 1499€ | 20%    | 3           |
| P003 (AirPods Pro 2)  | O006    | NEUF          | 279€  | 10%    | 20          |

## Architecture Hexagonale

```
        [ HTTP / Swagger ]
              ↓
    ┌─── EXPOSITION ─────────┐
    │  CartController        │  Adaptateur primaire (Driving)
    │  GlobalExceptionHandler│
    │  CartMapper            │
    └──────────┬─────────────┘
               │ appelle (via interfaces)
    ┌──────────▼─────────────┐
    │    APPLICATION         │
    │  AddItemToCartUseCase  │  Orchestration des use cases
    │  CheckoutUseCase       │  
    │  GetCartUseCase ...    │
    └──────────┬─────────────┘
               │ 
    ┌──────────▼─────────────┐
    │      DOMAIN            │  ← 
    │  Product, Offer, Cart  │  Entités + règles métier
    │  CartUseCases (ports in)│
    │  CartPorts (ports out) │  ← Interfaces que l'infra implémente
    └──────────▲─────────────┘
               │ implémente
    ┌──────────┴─────────────┐
    │   INFRASTRUCTURE       │
    │  InMemoryProductRepo   │ 
    │  InMemoryCartRepo      │
    └────────────────────────┘
```

## Choix techniques

- **Java 21**
- **Sping boot**
- **maven**
- **openApi**
