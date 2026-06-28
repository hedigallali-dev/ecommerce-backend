package com.ecommerce.infrastructure;

import com.ecommerce.domain.model.Offer;
import com.ecommerce.domain.model.OfferState;
import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.port.out.CartPorts;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryProductRepository implements CartPorts.ProductRepository {

    private final Map<String, Product> store = new HashMap<>();

    public InMemoryProductRepository() {
        // --- iPhone 15 Pro ---
        Product iphone = new Product("P001", "iPhone 15 Pro");
        iphone.addOffer(new Offer("O001", OfferState.NEUF, new BigDecimal("1199.00"), 0, 10));
        iphone.addOffer(new Offer("O002", OfferState.RECONDITIONNE, new BigDecimal("1199.00"), 15, 5));
        iphone.addOffer(new Offer("O003", OfferState.OCCASION, new BigDecimal("1199.00"), 30, 0)); // Stock 0 : test rupture
        store.put(iphone.productId(), iphone);

        // --- MacBook Air M3 ---
        Product macbook = new Product("P002", "MacBook Air M3");
        macbook.addOffer(new Offer("O004", OfferState.NEUF, new BigDecimal("1499.00"), 0, 8));
        macbook.addOffer(new Offer("O005", OfferState.RECONDITIONNE, new BigDecimal("1499.00"), 20, 3));
        store.put(macbook.productId(), macbook);

        // --- AirPods Pro 2 ---
        Product airpods = new Product("P003", "AirPods Pro 2");
        airpods.addOffer(new Offer("O006", OfferState.NEUF, new BigDecimal("279.00"), 10, 20));
        store.put(airpods.productId(), airpods);
    }

    @Override
    public Optional<Product> findById(String productId) {
        return Optional.ofNullable(store.get(productId));
    }
}
