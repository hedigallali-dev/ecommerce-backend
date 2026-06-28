package com.ecommerce.exposition.controller;

import com.ecommerce.domain.model.Cart;
import com.ecommerce.domain.model.Order;
import com.ecommerce.domain.port.in.CartUseCases;
import com.ecommerce.exposition.dto.CartDtos;
import com.ecommerce.exposition.mapper.CartMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}")
@Tag(name = "Panier", description = "Gestion du panier e-commerce multi-utilisateurs")
public class CartController {

    // Injection via les interfaces (ports d'entrée) — pas les classes concrètes
    private final CartUseCases.AddItemToCart addItemToCart;
    private final CartUseCases.UpdateCartItemQuantity updateCartItemQuantity;
    private final CartUseCases.RemoveCartItem removeCartItem;
    private final CartUseCases.GetCart getCart;
    private final CartUseCases.Checkout checkout;
    private final CartMapper mapper;

    public CartController(CartUseCases.AddItemToCart addItemToCart,
                          CartUseCases.UpdateCartItemQuantity updateCartItemQuantity,
                          CartUseCases.RemoveCartItem removeCartItem,
                          CartUseCases.GetCart getCart,
                          CartUseCases.Checkout checkout,
                          CartMapper mapper) {
        this.addItemToCart = addItemToCart;
        this.updateCartItemQuantity = updateCartItemQuantity;
        this.removeCartItem = removeCartItem;
        this.getCart = getCart;
        this.checkout = checkout;
        this.mapper = mapper;
    }

    @GetMapping("/cart")
    @Operation(
            summary = "Consulter le panier",
            description = "Retourne le contenu du panier avec les prix unitaires (après réduction) et le total."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Panier retourné avec succès"),
    })
    public ResponseEntity<CartDtos.CartResponse> getCart(
            @Parameter(description = "Identifiant de l'utilisateur", example = "alice")
            @PathVariable String userId) {

        Cart cart = getCart.execute(userId);
        return ResponseEntity.ok(mapper.toCartResponse(cart));
    }

    @PostMapping("/cart/items")
    @Operation(
            summary = "Ajouter un article au panier",
            description = "Ajoute un article ou incrémente la quantité si déjà présent. Échoue si le stock est insuffisant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Article ajouté, panier retourné"),
            @ApiResponse(responseCode = "404", description = "Produit ou offre introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Stock insuffisant",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<CartDtos.CartResponse> addItem(
            @Parameter(description = "Identifiant de l'utilisateur", example = "alice")
            @PathVariable String userId,
            @RequestBody CartDtos.AddItemRequest request) {

        Cart cart = addItemToCart.execute(userId, request.productId(), request.offerId(), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toCartResponse(cart));
    }

    @PutMapping("/cart/items/{productId}/{offerId}")
    @Operation(
            summary = "Modifier la quantité d'un article",
            description = "Met à jour la quantité d'un article existant dans le panier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quantité mise à jour, panier retourné"),
            @ApiResponse(responseCode = "404", description = "Article non trouvé dans le panier",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Stock insuffisant pour la nouvelle quantité",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<CartDtos.CartResponse> updateQuantity(
            @Parameter(description = "Identifiant de l'utilisateur", example = "alice")
            @PathVariable String userId,
            @Parameter(description = "Identifiant du produit", example = "P001")
            @PathVariable String productId,
            @Parameter(description = "Identifiant de l'offre", example = "O001")
            @PathVariable String offerId,
            @RequestBody CartDtos.UpdateQuantityRequest request) {

        Cart cart = updateCartItemQuantity.execute(userId, productId, offerId, request.quantity());
        return ResponseEntity.ok(mapper.toCartResponse(cart));
    }

    @DeleteMapping("/cart/items/{productId}/{offerId}")
    @Operation(
            summary = "Supprimer un article du panier",
            description = "Retire définitivement un article du panier de l'utilisateur."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Article supprimé, panier retourné"),
            @ApiResponse(responseCode = "404", description = "Article non trouvé dans le panier",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<CartDtos.CartResponse> removeItem(
            @Parameter(description = "Identifiant de l'utilisateur", example = "alice")
            @PathVariable String userId,
            @Parameter(description = "Identifiant du produit", example = "P001")
            @PathVariable String productId,
            @Parameter(description = "Identifiant de l'offre", example = "O001")
            @PathVariable String offerId) {

        Cart cart = removeCartItem.execute(userId, productId, offerId);
        return ResponseEntity.ok(mapper.toCartResponse(cart));
    }

    @PostMapping("/checkout")
    @Operation(
            summary = "Passer commande (checkout)",
            description = "Valide le panier, décrémente les stocks et crée une commande. Le panier est vidé après succès."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Commande créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Panier vide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Stock insuffisant au moment du checkout",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<CartDtos.OrderResponse> checkout(
            @Parameter(description = "Identifiant de l'utilisateur", example = "alice")
            @PathVariable String userId) {

        Order order = checkout.execute(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toOrderResponse(order));
    }

    @Schema(description = "Réponse d'erreur standard")
    record ErrorResponse(
            @Schema(description = "Message d'erreur", example = "Produit introuvable : P999")
            String error,
            @Schema(description = "Code HTTP", example = "404")
            int status,
            @Schema(description = "Horodatage ISO 8601", example = "2024-06-01T14:30:00Z")
            String timestamp
    ) {
    }
}
