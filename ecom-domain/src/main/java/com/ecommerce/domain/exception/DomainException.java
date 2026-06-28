package com.ecommerce.domain.exception;

/**
 * Exception de base du domaine.
 * Toutes les violations de règles métier héritent de cette classe.
 * <p>
 * On étend RuntimeException car les erreurs métier ne doivent pas être
 * forcément catchées partout (unchecked), mais restent identifiables.
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
