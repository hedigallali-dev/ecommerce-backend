package com.ecommerce.domain.model;

import com.ecommerce.domain.exception.DomainException;


public class CartItem {

    private final String productId;
    private final String offerId;
    private int quantity;

    public CartItem(String productId, String offerId, int quantity) {
        validateQuantity(quantity);
        this.productId = productId;
        this.offerId = offerId;
        this.quantity = quantity;
    }

    public void updateQuantity(int newQty) {
        validateQuantity(newQty);
        this.quantity = newQty;
    }

    private void validateQuantity(int qty) {
        if (qty <= 0) {
            throw new DomainException("La quantité doit être strictement positive, reçu : " + qty);
        }
    }

    public String productId() {
        return productId;
    }

    public String offerId() {
        return offerId;
    }

    public int quantity() {
        return quantity;
    }
}
