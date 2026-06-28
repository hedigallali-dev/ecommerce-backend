package com.ecommerce.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {

    private final String userId;
    private final List<CartItem> items;

    public Cart(String userId) {
        this.userId = userId;
        this.items = new ArrayList<>();
    }

    public Optional<CartItem> findItem(String productId, String offerId) {
        return items.stream()
                .filter(i -> i.productId().equals(productId) && i.offerId().equals(offerId))
                .findFirst();
    }

    public void addItem(CartItem item) {
        items.add(item);
    }

    public boolean removeItem(String productId, String offerId) {
        return items.removeIf(i -> i.productId().equals(productId) && i.offerId().equals(offerId));
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public String userId() {
        return userId;
    }

    public List<CartItem> items() {
        return List.copyOf(items);
    } // Copie défensive
}
