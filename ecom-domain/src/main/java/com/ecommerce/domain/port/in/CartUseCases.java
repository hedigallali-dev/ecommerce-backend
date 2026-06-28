package com.ecommerce.domain.port.in;

import com.ecommerce.domain.model.Cart;
import com.ecommerce.domain.model.Order;

public class CartUseCases {

    /**
     * Cas d'usage : Ajouter un article au panier.
     */
    public interface AddItemToCart {
        Cart execute(String userId, String productId, String offerId, int quantity);
    }

    /**
     * Cas d'usage : Modifier la quantité d'un article dans le panier.
     */
    public interface UpdateCartItemQuantity {
        Cart execute(String userId, String productId, String offerId, int newQuantity);
    }

    /**
     * Cas d'usage : Supprimer un article du panier.
     */
    public interface RemoveCartItem {
        Cart execute(String userId, String productId, String offerId);
    }

    /**
     * Consulter le panier d'un utilisateur : Crée un panier vide si l'utilisateur n'en a pas encore.
     */
    public interface GetCart {
        Cart execute(String userId);
    }

    /**
     * Passer commande (checkout) : Décrémente les stocks et vide le panier.
     */
    public interface Checkout {
        Order execute(String userId);
    }
}
