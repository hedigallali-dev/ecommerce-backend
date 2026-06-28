package com.ecommerce.domain.exception;

/**
 * Exceptions métier spécifiques.
 * <p>
 * Chaque cas d'erreur métier a sa propre exception :
 * → L'exposition peut mapper précisément chaque exception à un code HTTP.
 * → Le code appelant comprend immédiatement ce qui s'est passé.
 */
public class CartDomainExceptions {

    /**
     * Ressource introuvable (produit, offre, article dans le panier)
     */
    public static class NotFoundException extends DomainException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Tentative d'achat d'un article sans stock suffisant
     */
    public static class OutOfStockException extends DomainException {
        public OutOfStockException(String message) {
            super(message);
        }
    }

    /**
     * Tentative de checkout avec un panier vide
     */
    public static class EmptyCartException extends DomainException {
        public EmptyCartException(String message) {
            super(message);
        }
    }
}
