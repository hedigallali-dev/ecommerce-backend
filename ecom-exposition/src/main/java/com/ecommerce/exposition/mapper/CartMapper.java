package com.ecommerce.exposition.mapper;

import com.ecommerce.domain.exception.CartDomainExceptions;
import com.ecommerce.domain.model.Cart;
import com.ecommerce.domain.model.CartItem;
import com.ecommerce.domain.model.Offer;
import com.ecommerce.domain.model.Order;
import com.ecommerce.domain.port.out.CartPorts;
import com.ecommerce.exposition.dto.CartDtos;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartMapper {

    private final CartPorts.ProductRepository productRepository;

    public CartMapper(CartPorts.ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public CartDtos.CartResponse toCartResponse(Cart cart) {
        List<CartDtos.CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal cartTotal = BigDecimal.ZERO;

        for (CartItem item : cart.items()) {
            // Récupérer le produit et l'offre pour le libellé et le prix
            var product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new CartDomainExceptions.NotFoundException(
                            "Produit introuvable lors du mapping : " + item.productId()));
            Offer offer = product.findOffer(item.offerId())
                    .orElseThrow(() -> new CartDomainExceptions.NotFoundException(
                            "Offre introuvable lors du mapping : " + item.offerId()));

            BigDecimal unitPrice = offer.effectivePrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity()));

            itemResponses.add(new CartDtos.CartItemResponse(
                    item.productId(),
                    product.label(),
                    item.offerId(),
                    offer.state().name(),
                    item.quantity(),
                    unitPrice,
                    lineTotal
            ));

            cartTotal = cartTotal.add(lineTotal);
        }

        return new CartDtos.CartResponse(cart.userId(), itemResponses, cartTotal);
    }

    public CartDtos.OrderResponse toOrderResponse(Order order) {
        return new CartDtos.OrderResponse(
                order.orderId(),
                order.userId(),
                order.totalPrice(),
                order.createdAt().toString(),
                order.items().size()
        );
    }
}
