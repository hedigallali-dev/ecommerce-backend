package com.ecommerce.domain.model;

import com.ecommerce.domain.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Offer {

    private final String offerId;
    private final OfferState state;
    private final BigDecimal price;
    private final int discountPercent;
    private int stockQty;

    public Offer(String offerId, OfferState state, BigDecimal price, int discountPercent, int stockQty) {
        if (discountPercent < 0 || discountPercent > 100) {
            throw new DomainException("La réduction doit être comprise entre 0 et 100%, reçu : " + discountPercent);
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Le prix doit être positif ou nul");
        }
        if (stockQty < 0) {
            throw new DomainException("Le stock ne peut pas être négatif");
        }
        this.offerId = offerId;
        this.state = state;
        this.price = price;
        this.discountPercent = discountPercent;
        this.stockQty = stockQty;
    }

    /**
     * RÈGLE MÉTIER : calcul du prix effectif après réduction.
     * Exemple : 1199.00 avec 15% → 1199.00 × 0.85 = 1019.15
     * RoundingMode.HALF_UP = arrondi bancaire standard (0.5 → 1).
     */
    public BigDecimal effectivePrice() {
        BigDecimal factor = BigDecimal.ONE
                .subtract(BigDecimal.valueOf(discountPercent).divide(BigDecimal.valueOf(100)));
        return price.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * RÈGLE MÉTIER : vérifier la disponibilité du stock.
     */
    public boolean hasEnoughStock(int requestedQty) {
        return stockQty >= requestedQty;
    }

    /**
     * RÈGLE MÉTIER : décrémenter le stock lors d'un achat.
     * Lève une exception domaine si le stock est insuffisant.
     */
    public void decrementStock(int qty) {
        if (!hasEnoughStock(qty)) {
            throw new DomainException(
                    "Stock insuffisant pour l'offre '%s' : demandé=%d, disponible=%d"
                            .formatted(offerId, qty, stockQty));
        }
        this.stockQty -= qty;
    }

    public String offerId() {
        return offerId;
    }

    public OfferState state() {
        return state;
    }

    public BigDecimal price() {
        return price;
    }

    public int discountPercent() {
        return discountPercent;
    }

    public int stockQty() {
        return stockQty;
    }
}
