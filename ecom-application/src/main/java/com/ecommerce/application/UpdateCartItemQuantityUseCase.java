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
 * CAS D'USAGE : Modifier la quantité d'un article
 */
@Service
public class UpdateCartItemQuantityUseCase implements CartUseCases.UpdateCartItemQuantity {

    private final CartPorts.ProductRepository productRepository;
    private final CartPorts.CartRepository cartRepository;

    public UpdateCartItemQuantityUseCase(CartPorts.ProductRepository productRepository,
                                         CartPorts.CartRepository cartRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public Cart execute(String userId, String productId, String offerId, int newQuantity) {
        Cart cart = cartRepository.getOrCreate(userId);

        // L'article doit être dans le panier
        CartItem item = cart.findItem(productId, offerId)
                .orElseThrow(() -> new CartDomainExceptions.NotFoundException(
                        "Article non trouvé dans le panier : %s / %s".formatted(productId, offerId)));

        // Vérifier le stock pour la nouvelle quantité
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CartDomainExceptions.NotFoundException("Produit introuvable : " + productId));
        Offer offer = product.findOffer(offerId)
                .orElseThrow(() -> new CartDomainExceptions.NotFoundException("Offre introuvable : " + offerId));

        if (!offer.hasEnoughStock(newQuantity)) {
            throw new CartDomainExceptions.OutOfStockException(
                    "Stock insuffisant : demandé=%d, disponible=%d".formatted(newQuantity, offer.stockQty()));
        }

        // La validation de la quantité est dans l'entité CartItem (règle métier)
        item.updateQuantity(newQuantity);
        cartRepository.save(cart);
        return cart;
    }
}
