package com.ecommerce.application;

import com.ecommerce.domain.exception.CartDomainExceptions;
import com.ecommerce.domain.model.*;
import com.ecommerce.domain.port.out.CartPorts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseCaseTest {

    @Mock
    CartPorts.ProductRepository productRepository;
    @Mock
    CartPorts.CartRepository cartRepository;

    private AddItemToCartUseCase addItemUseCase;
    private CheckoutUseCase checkoutUseCase;
    private RemoveCartItemUseCase removeItemUseCase;
    private GetCartUseCase getCartUseCase;

    private Product product;
    private Offer offerWithStock;
    private Offer offerWithoutStock;
    private Cart emptyCart;

    @BeforeEach
    void setUp() {
        addItemUseCase = new AddItemToCartUseCase(productRepository, cartRepository);
        checkoutUseCase = new CheckoutUseCase(cartRepository, productRepository);
        removeItemUseCase = new RemoveCartItemUseCase(cartRepository);
        getCartUseCase = new GetCartUseCase(cartRepository);

        offerWithStock = new Offer("O001", OfferState.NEUF, new BigDecimal("1199.00"), 0, 10);
        offerWithoutStock = new Offer("O002", OfferState.OCCASION, new BigDecimal("999.00"), 0, 0);
        product = new Product("P001", "iPhone 15 Pro");
        product.addOffer(offerWithStock);
        product.addOffer(offerWithoutStock);

        emptyCart = new Cart("alice");
    }

    @Test
    @DisplayName("Ajouter un article valide → article présent dans le panier retourné")
    void addItem_valid_returnsCartWithItem() {
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));
        when(cartRepository.getOrCreate("alice")).thenReturn(emptyCart);

        Cart result = addItemUseCase.execute("alice", "P001", "O001", 2);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).quantity()).isEqualTo(2);

        verify(cartRepository).save(emptyCart);
    }

    @Test
    @DisplayName("Ajouter un article en rupture → OutOfStockException")
    void addItem_outOfStock_throwsException() {
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));
        when(cartRepository.getOrCreate("alice")).thenReturn(emptyCart);

        assertThatThrownBy(() -> addItemUseCase.execute("alice", "P001", "O002", 1))
                .isInstanceOf(CartDomainExceptions.OutOfStockException.class);

        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("Produit inexistant → NotFoundException")
    void addItem_unknownProduct_throwsNotFoundException() {
        when(productRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addItemUseCase.execute("alice", "INCONNU", "O001", 1))
                .isInstanceOf(CartDomainExceptions.NotFoundException.class)
                .hasMessageContaining("INCONNU");
    }

    @Test
    @DisplayName("Ajouter deux fois le même article → les quantités s'additionnent")
    void addItem_sameItemTwice_aggregatesQuantities() {
        Cart cartWithItem = new Cart("alice");
        cartWithItem.addItem(new CartItem("P001", "O001", 2));

        when(productRepository.findById("P001")).thenReturn(Optional.of(product));
        when(cartRepository.getOrCreate("alice")).thenReturn(cartWithItem);

        Cart result = addItemUseCase.execute("alice", "P001", "O001", 3);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).quantity()).isEqualTo(5);
    }


    @Test
    @DisplayName("Checkout panier vide → EmptyCartException")
    void checkout_emptyCart_throwsEmptyCartException() {
        when(cartRepository.getOrCreate("alice")).thenReturn(emptyCart);

        assertThatThrownBy(() -> checkoutUseCase.execute("alice"))
                .isInstanceOf(CartDomainExceptions.EmptyCartException.class);
    }

    @Test
    @DisplayName("Checkout valide → commande créée, panier vidé, stock décrémenté")
    void checkout_validCart_createsOrderAndClearsCart() {

        Cart cart = new Cart("alice");
        cart.addItem(new CartItem("P001", "O001", 2));

        when(cartRepository.getOrCreate("alice")).thenReturn(cart);
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));

        var order = checkoutUseCase.execute("alice");


        assertThat(order.orderId()).isNotNull();
        assertThat(order.totalPrice()).isEqualByComparingTo(new BigDecimal("2398.00")); // 1199 × 2
        assertThat(order.items()).hasSize(1);


        assertThat(cart.isEmpty()).isTrue();

        assertThat(offerWithStock.stockQty()).isEqualTo(8);
    }

    @Test
    @DisplayName("Supprimer un article existant → panier vide")
    void removeItem_existingItem_removesIt() {
        Cart cart = new Cart("alice");
        cart.addItem(new CartItem("P001", "O001", 1));
        when(cartRepository.getOrCreate("alice")).thenReturn(cart);

        Cart result = removeItemUseCase.execute("alice", "P001", "O001");

        assertThat(result.isEmpty()).isTrue();
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("Supprimer un article absent → NotFoundException")
    void removeItem_notInCart_throwsNotFoundException() {
        when(cartRepository.getOrCreate("alice")).thenReturn(emptyCart);

        assertThatThrownBy(() -> removeItemUseCase.execute("alice", "P001", "O001"))
                .isInstanceOf(CartDomainExceptions.NotFoundException.class);
    }

    @Test
    @DisplayName("Consulter le panier → retourne le panier de l'utilisateur")
    void getCart_returnsUserCart() {
        when(cartRepository.getOrCreate("alice")).thenReturn(emptyCart);

        Cart result = getCartUseCase.execute("alice");

        assertThat(result.userId()).isEqualTo("alice");
        verify(cartRepository).getOrCreate("alice");
    }
}
