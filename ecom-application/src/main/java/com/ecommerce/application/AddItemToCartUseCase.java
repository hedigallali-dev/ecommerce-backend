package com.ecommerce.application;

import com.ecommerce.domain.exception.CartDomainExceptions;
import com.ecommerce.domain.model.Cart;
import com.ecommerce.domain.model.CartItem;
import com.ecommerce.domain.model.Offer;
import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.port.in.CartUseCases;
import com.ecommerce.domain.port.out.CartPorts;
import org.springframework.stereotype.Service;

/**
 * USE CASE : Ajouter un article au panier
 * 1. Valide l'existence du produit et de l'offre
 * 2. Vérifie le stock disponible (en tenant compte de ce qui est déjà dans le panier)
 * 3. Ajoute ou incrémente la quantité dans le panier
 * 4. Persiste le panier
 */
@Service
public class AddItemToCartUseCase implements CartUseCases.AddItemToCart {

    private final CartPorts.ProductRepository productRepository;
    private final CartPorts.CartRepository cartRepository;

    public AddItemToCartUseCase(CartPorts.ProductRepository productRepository,
                                CartPorts.CartRepository cartRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public Cart execute(String userId, String productId, String offerId, int quantity) {

        // 1. Le produit existe-t-il ?
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CartDomainExceptions.NotFoundException(
                        "Produit introuvable : " + productId));

        // 2. L'offre existe-t-elle dans ce produit ?
        Offer offer = product.findOffer(offerId)
                .orElseThrow(() -> new CartDomainExceptions.NotFoundException(
                        "Offre introuvable '%s' pour le produit '%s'".formatted(offerId, productId)));

        // 3. Vérifier le stock (quantité déjà dans le panier + nouvelle quantité)
        Cart cart = cartRepository.getOrCreate(userId);
        int alreadyInCart = cart.findItem(productId, offerId)
                .map(CartItem::quantity)
                .orElse(0);
        int totalRequested = alreadyInCart + quantity;

        if (!offer.hasEnoughStock(totalRequested)) {
            throw new CartDomainExceptions.OutOfStockException(
                    "Stock insuffisant pour l'offre '%s' : demandé=%d, disponible=%d"
                            .formatted(offerId, totalRequested, offer.stockQty()));
        }

        // 4. Ajouter ou incrémenter
        cart.findItem(productId, offerId).ifPresentOrElse(
                existing -> existing.updateQuantity(totalRequested),       // Article déjà présent → on cumule
                () -> cart.addItem(new CartItem(productId, offerId, quantity)) // Nouvel article
        );

        cartRepository.save(cart);
        return cart;
    }
}
