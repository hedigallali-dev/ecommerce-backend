package com.ecommerce.application;

import com.ecommerce.domain.exception.CartDomainExceptions;
import com.ecommerce.domain.model.Cart;
import com.ecommerce.domain.port.in.CartUseCases;
import com.ecommerce.domain.port.out.CartPorts;
import org.springframework.stereotype.Service;

/**
 * CAS D'USAGE : Supprimer un article du panier
 */
@Service
public class RemoveCartItemUseCase implements CartUseCases.RemoveCartItem {

    private final CartPorts.CartRepository cartRepository;

    public RemoveCartItemUseCase(CartPorts.CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public Cart execute(String userId, String productId, String offerId) {
        Cart cart = cartRepository.getOrCreate(userId);

        // removeItem retourne false si l'article n'était pas dans le panier
        boolean removed = cart.removeItem(productId, offerId);
        if (!removed) {
            throw new CartDomainExceptions.NotFoundException(
                    "Article non trouvé dans le panier : %s / %s".formatted(productId, offerId));
        }

        cartRepository.save(cart);
        return cart;
    }
}
