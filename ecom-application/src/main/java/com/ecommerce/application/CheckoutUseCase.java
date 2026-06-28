package com.ecommerce.application;

import com.ecommerce.domain.exception.CartDomainExceptions;
import com.ecommerce.domain.model.*;
import com.ecommerce.domain.port.in.CartUseCases;
import com.ecommerce.domain.port.out.CartPorts;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * USE CASE : Checkout (passer commande)
 * =========================================
 * <p>
 * Phase 1 : VALIDATION (on parcour tout le panier sans modifier le stock)
 * → Si une erreur survient, rien n'a été modifié (principe transactionnel)
 * <p>
 * Phase 2 : EXÉCUTION (on décrémente les stocks seulement si tout est OK)
 */
@Service
public class CheckoutUseCase implements CartUseCases.Checkout {

    private final CartPorts.CartRepository cartRepository;
    private final CartPorts.ProductRepository productRepository;

    public CheckoutUseCase(CartPorts.CartRepository cartRepository,
                           CartPorts.ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Order execute(String userId) {
        Cart cart = cartRepository.getOrCreate(userId);

        // 1. Panier vide ?
        if (cart.isEmpty()) {
            throw new CartDomainExceptions.EmptyCartException(
                    "Impossible de passer commande : le panier de '%s' est vide".formatted(userId));
        }

        // === PHASE 1 : VALIDATION COMPLÈTE ===
        // On calcule le total et valide les stocks AVANT de toucher quoi que ce soit
        BigDecimal total = BigDecimal.ZERO;
        // On construit des paires (CartItem, Offer) pour ne pas re-chercher en phase 2
        record ItemWithOffer(CartItem item, Offer offer) {
        }
        List<ItemWithOffer> validated = new ArrayList<>();

        for (CartItem item : cart.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new CartDomainExceptions.NotFoundException(
                            "Produit introuvable au checkout : " + item.productId()));
            Offer offer = product.findOffer(item.offerId())
                    .orElseThrow(() -> new CartDomainExceptions.NotFoundException(
                            "Offre introuvable au checkout : " + item.offerId()));

            // Revalider le stock au moment du checkout (il peut avoir changé depuis l'ajout)
            if (!offer.hasEnoughStock(item.quantity())) {
                throw new CartDomainExceptions.OutOfStockException(
                        "Stock insuffisant au checkout pour '%s' : demandé=%d, disponible=%d"
                                .formatted(item.offerId(), item.quantity(), offer.stockQty()));
            }

            total = total.add(offer.effectivePrice().multiply(BigDecimal.valueOf(item.quantity())));
            validated.add(new ItemWithOffer(item, offer));
        }

        // === PHASE 2 : EXÉCUTION (tout est OK, on modifie) ===
        // Snapshot des articles avant de vider le panier
        List<CartItem> snapshot = validated.stream()
                .map(iwo -> new CartItem(iwo.item().productId(), iwo.item().offerId(), iwo.item().quantity()))
                .toList();

        // Décrémenter les stocks
        validated.forEach(iwo -> iwo.offer().decrementStock(iwo.item().quantity()));

        // Créer la commande avec un UUID unique
        Order order = new Order(
                UUID.randomUUID().toString(),
                userId,
                snapshot,
                total,
                Instant.now()
        );

        // Vider le panier après succès
        cart.clear();
        cartRepository.save(cart);

        return order;
    }
}
