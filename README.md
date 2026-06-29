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
    │  CheckoutUseCase       │  Implémente les ports d'entrée
    │  GetCartUseCase ...    │
    └──────────┬─────────────┘
               │ dépend de
    ┌──────────▼─────────────┐
    │      DOMAIN            │  ← 
    │  Product, Offer, Cart  │  Entités + règles métier
    │  CartUseCases (ports in)│
    │  CartPorts (ports out) │  ← Interfaces que l'infra implémente
    └──────────▲─────────────┘
               │ implémente
    ┌──────────┴─────────────┐
    │   INFRASTRUCTURE       │
    │  InMemoryProductRepo   │  Adaptateur secondaire (Driven)
    │  InMemoryCartRepo      │
    └────────────────────────┘
```

### Pourquoi "in" et "out" dans les ports ?

Les ports ont deux directions opposées selon qui appelle qui :

**`port/in` = ports d'ENTRÉE** — quelqu'un de l'extérieur appelle le Domain

```
[Controller HTTP] ──appelle──▶ [port/in : AddItemToCart] ──▶ [Use Case]
```

Le Controller ne connaît pas `AddItemToCartUseCase` directement. Il connaît uniquement
l'interface `CartUseCases.AddItemToCart`.

**`port/out` = ports de SORTIE** — le Domain appelle vers l'extérieur

```
[Use Case] ──appelle──▶ [port/out : ProductRepository] ──▶ [InMemoryProductRepository]
```

Le Use Case a besoin de données mais ne sait pas comment elles sont stockées.
Il appelle l'interface, l'Infrastructure répond.

|                | port/in                 | port/out                    |
|----------------|-------------------------|-----------------------------|
| Défini dans    | Domain                  | Domain                      |
| Appelé par     | Exposition (Controller) | Application (Use Case)      |
| Implémenté par | Application (Use Case)  | Infrastructure (Repository) |
| Direction      | Vers l'intérieur        | Vers l'extérieur            |

---

### Pourquoi des "Use Cases" dans Application ?

Un **Use Case** (cas d'usage) représente **une action qu'un utilisateur peut faire avec le
système**.

Au lieu d'avoir un gros `CartService` avec 10 méthodes, on découpe en classes à
**responsabilité unique** :

```java
// ❌ Approche classique : un seul service "fourre-tout"
class CartService {
    CartResponse addItem(...) { ...}

    CartResponse removeItem(...) { ...}

    CartResponse getCart(...) { ...}

    OrderResponse checkout(...) { ...}
    // + méthodes privées, mappings... → classe de 200 lignes
}

// ✅ Approche hexagonale : une classe = une action métier
class AddItemToCartUseCase { ...
}  // 50 lignes, une seule responsabilité

class RemoveCartItemUseCase { ...
}  // 30 lignes

class CheckoutUseCase { ...
}  // 60 lignes

class GetCartUseCase { ...
}  // 15 lignes
```

---

### Pourquoi une méthode `execute()` ?

Parce que chaque Use Case implémente une **interface à une seule méthode**
(définie dans `port/in`) :

```java
// Dans le Domain (port/in)
public interface AddItemToCart {
    Cart execute(String userId, String productId, String offerId, int quantity);
}

// Dans l'Application
public class AddItemToCartUseCase implements AddItemToCart {
    @Override
    public Cart execute(...) { ...}
}
```

La méthode s'appelle `execute` pour deux raisons :

**Raison 1 — L'interface a une seule responsabilité**, donc une seule méthode.
`execute` dit clairement "déclenche ce cas d'usage". C'est le pattern **Command** :
un objet = une action.

**Raison 2 — Le Controller est découplé du nom métier.** Il appelle toujours `.execute(...)`,
quelle que soit l'action.

---
## Choix techniques

- **Java 21**
- **Sping boot**
- **maven**
- **openApi**

---

## Autres choix techniques

- **BigDecimal** pour les prix : évite les erreurs d'arrondi des `float`/`double` (0.1 + 0.2 ≠ 0.3 en float)
- **Records Java 21** pour les DTOs et `Order` : immuabilité garantie
- **Two-phase checkout** : validation complète de tous les stocks AVANT toute décrémentation
- **Tests Domain** : Java, sans Spring, sans mockito
- **Tests Application** : Mockito pour simuler les ports de sortie et tester l'orchestration seule
