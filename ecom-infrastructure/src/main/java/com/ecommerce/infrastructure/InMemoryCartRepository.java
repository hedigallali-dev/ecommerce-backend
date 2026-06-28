package com.ecommerce.infrastructure;

import com.ecommerce.domain.model.Cart;
import com.ecommerce.domain.port.out.CartPorts;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryCartRepository implements CartPorts.CartRepository {

    private final Map<String, Cart> store = new HashMap<>();

    @Override
    public Cart getOrCreate(String userId) {
        return store.computeIfAbsent(userId, Cart::new);
    }

    @Override
    public void save(Cart cart) {
        store.put(cart.userId(), cart);
    }
}
