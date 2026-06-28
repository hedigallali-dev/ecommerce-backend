package com.ecommerce.exposition.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;


public class CartDtos {

    @Schema(description = "Requête pour ajouter un article au panier")
    public record AddItemRequest(
            @Schema(description = "Identifiant du produit", example = "P001", requiredMode = Schema.RequiredMode.REQUIRED)
            String productId,

            @Schema(description = "Identifiant de l'offre", example = "O001", requiredMode = Schema.RequiredMode.REQUIRED)
            String offerId,

            @Schema(description = "Quantité souhaitée (≥ 1)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
            int quantity
    ) {
    }

    @Schema(description = "Requête pour modifier la quantité d'un article")
    public record UpdateQuantityRequest(
            @Schema(description = "Nouvelle quantité (≥ 1)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
            int quantity
    ) {
    }

    @Schema(description = "Détail d'une ligne dans le panier")
    public record CartItemResponse(
            @Schema(description = "Identifiant du produit", example = "P001")
            String productId,

            @Schema(description = "Libellé du produit", example = "iPhone 15 Pro")
            String productLabel,

            @Schema(description = "Identifiant de l'offre", example = "O001")
            String offerId,

            @Schema(description = "État de l'article", example = "NEUF", allowableValues = {"NEUF", "OCCASION", "RECONDITIONNE"})
            String offerState,

            @Schema(description = "Quantité dans le panier", example = "2")
            int quantity,

            @Schema(description = "Prix unitaire après réduction (€)", example = "1019.15")
            BigDecimal unitPrice,

            @Schema(description = "Sous-total de la ligne (unitPrice × quantity)", example = "2038.30")
            BigDecimal lineTotal
    ) {
    }

    @Schema(description = "Contenu complet du panier utilisateur")
    public record CartResponse(
            @Schema(description = "Identifiant de l'utilisateur", example = "alice")
            String userId,

            @Schema(description = "Liste des articles dans le panier")
            List<CartItemResponse> items,

            @Schema(description = "Total du panier toutes lignes confondues (€)", example = "2398.00")
            BigDecimal totalPrice
    ) {
    }

    @Schema(description = "Confirmation de commande après checkout")
    public record OrderResponse(
            @Schema(description = "Identifiant unique de la commande (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            String orderId,

            @Schema(description = "Identifiant de l'utilisateur", example = "alice")
            String userId,

            @Schema(description = "Montant total de la commande (€)", example = "2398.00")
            BigDecimal totalPrice,

            @Schema(description = "Horodatage ISO 8601 de la commande", example = "2024-06-01T14:30:00Z")
            String createdAt,

            @Schema(description = "Nombre de lignes commandées", example = "2")
            int itemCount
    ) {
    }
}
