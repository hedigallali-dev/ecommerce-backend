package com.ecommerce.domain.model;

import com.ecommerce.domain.exception.DomainException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Product {

    private final String productId;
    private final String label;
    private final List<Offer> offers;

    public Product(String productId, String label) {
        if (productId == null || productId.isBlank()) {
            throw new DomainException("L'identifiant produit est obligatoire");
        }
        this.productId = productId;
        this.label = label;
        this.offers = new ArrayList<>();
    }

    public void addOffer(Offer offer) {
        offers.add(offer);
    }

    public Optional<Offer> findOffer(String offerId) {
        return offers.stream()
                .filter(o -> o.offerId().equals(offerId))
                .findFirst();
    }

    public String productId() {
        return productId;
    }

    public String label() {
        return label;
    }

    public List<Offer> offers() {
        return List.copyOf(offers);
    }
}
