package com.ecommerce.domain.port.out;

import com.ecommerce.domain.model.Cart;
import com.ecommerce.domain.model.Product;

import java.util.Optional;

public class CartPorts {

    /**
     * accès aux produits.
     */
    public interface ProductRepository {
        Optional<Product> findById(String productId);
    }

    /**
     * accès au stockage des paniers.
     */
    public interface CartRepository {
        Cart getOrCreate(String userId);

        void save(Cart cart);
    }
}
