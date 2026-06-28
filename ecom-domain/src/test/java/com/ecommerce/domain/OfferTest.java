package com.ecommerce.domain;

import com.ecommerce.domain.exception.DomainException;
import com.ecommerce.domain.model.Offer;
import com.ecommerce.domain.model.OfferState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OfferTest {

    private Offer newOffer(int discountPercent, int stock) {
        return new Offer("O1", OfferState.NEUF, new BigDecimal("1000.00"), discountPercent, stock);
    }

    @Test
    @DisplayName("Prix effectif sans réduction = prix de base")
    void effectivePrice_noDiscount_returnsBasePrice() {
        Offer offer = newOffer(0, 10);
        assertThat(offer.effectivePrice()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Prix effectif avec 20% de réduction = 800.00")
    void effectivePrice_twentyPercentDiscount_returnsReducedPrice() {
        Offer offer = newOffer(20, 10);
        assertThat(offer.effectivePrice()).isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    @DisplayName("Prix effectif avec 15% sur 1199€ = 1019.15")
    void effectivePrice_fifteenPercentOn1199_returns1019_15() {
        Offer offer = new Offer("O1", OfferState.RECONDITIONNE, new BigDecimal("1199.00"), 15, 5);
        assertThat(offer.effectivePrice()).isEqualByComparingTo(new BigDecimal("1019.15"));
    }

    @Test
    @DisplayName("hasEnoughStock : stock suffisant → true")
    void hasEnoughStock_sufficientStock_returnsTrue() {
        assertThat(newOffer(0, 5).hasEnoughStock(3)).isTrue();
    }

    @Test
    @DisplayName("hasEnoughStock : stock exact → true")
    void hasEnoughStock_exactStock_returnsTrue() {
        assertThat(newOffer(0, 5).hasEnoughStock(5)).isTrue();
    }

    @Test
    @DisplayName("hasEnoughStock : stock 0 → false")
    void hasEnoughStock_zeroStock_returnsFalse() {
        assertThat(newOffer(0, 0).hasEnoughStock(1)).isFalse();
    }

    @Test
    @DisplayName("decrementStock : décrémente correctement le stock")
    void decrementStock_validQty_reducesStock() {
        Offer offer = newOffer(0, 10);
        offer.decrementStock(3);
        assertThat(offer.stockQty()).isEqualTo(7);
    }

    @Test
    @DisplayName("decrementStock : stock insuffisant → DomainException")
    void decrementStock_insufficientStock_throwsDomainException() {
        Offer offer = newOffer(0, 2);
        assertThatThrownBy(() -> offer.decrementStock(5))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Construction avec discount > 100 → DomainException")
    void constructor_invalidDiscount_throwsDomainException() {
        assertThatThrownBy(() -> new Offer("O1", OfferState.NEUF, new BigDecimal("100"), 101, 5))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("réduction");
    }

    @Test
    @DisplayName("Construction avec stock négatif → DomainException")
    void constructor_negativeStock_throwsDomainException() {
        assertThatThrownBy(() -> new Offer("O1", OfferState.NEUF, new BigDecimal("100"), 0, -1))
                .isInstanceOf(DomainException.class);
    }
}
