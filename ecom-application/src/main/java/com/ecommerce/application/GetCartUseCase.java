package com.ecommerce.application;

import com.ecommerce.domain.model.Cart;
import com.ecommerce.domain.port.in.CartUseCases;
import com.ecommerce.domain.port.out.CartPorts;
import org.springframework.stereotype.Service;

/**
 * USE CASE : Consulter le panier
 * Crée un panier vide si l'utilisateur n'en a pas encore.
 */
@Service
public class GetCartUseCase implements CartUseCases.GetCart {

    private final CartPorts.CartRepository cartRepository;

    public GetCartUseCase(CartPorts.CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public Cart execute(String userId) {
        return cartRepository.getOrCreate(userId);
    }
}
