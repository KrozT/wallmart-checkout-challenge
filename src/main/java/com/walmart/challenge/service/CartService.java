package com.walmart.challenge.service;

import com.walmart.challenge.dto.*;
import com.walmart.challenge.entity.Cart;
import com.walmart.challenge.entity.CartItem;
import com.walmart.challenge.entity.Product;
import com.walmart.challenge.entity.ShippingAddress;
import com.walmart.challenge.repository.CartRepository;
import com.walmart.challenge.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for the lifecycle management of shopping carts.
 * It handles creation, item modification, and retrieval.
 * Business logic regarding item manipulation and identifier generation is centralized here.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    /**
     * Initializes and persists a new cart with the provided shipping details.
     * The cart identifier is generated internally by the persistence layer.
     *
     * @param request The initialization data for the cart.
     * @return The persisted Cart entity.
     */
    @Transactional
    public Cart createCart(CreateCartRequest request) {
        log.info("Initializing new shopping cart session");

        Cart cart = new Cart();

        if (request.getShippingAddress() != null) {
            ShippingAddressDto addrReq = request.getShippingAddress();
            ShippingAddress address = new ShippingAddress();
            address.setStreet(addrReq.getStreet());
            address.setCity(addrReq.getCity());
            address.setZoneId(addrReq.getZoneId());
            cart.setShippingAddress(address);
        }

        return cartRepository.save(cart);
    }

    /**
     * Adds a product to the cart or updates the quantity if it already exists.
     * This method handles the relationship management between Cart and CartItem.
     *
     * @param cartId  The unique identifier of the cart (UUID).
     * @param request The item details (SKU and Quantity).
     * @return The updated Cart entity.
     */
    @Transactional
    public Cart addItem(String cartId, AddItemRequest request) {
        log.info("Request to add {} units of SKU {} to cart {}", request.getQuantity(), request.getSku(), cartId);

        UUID uuid = UUID.fromString(cartId);
        Cart cart = cartRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

        Product product = productRepository.findBySku(request.getSku())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.getSku()));

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existing = existingItemOpt.get();
            int newQuantity = existing.getQuantity() + request.getQuantity();
            existing.setQuantity(newQuantity);
            log.debug("Incremented quantity of SKU {} in cart {} to {}", request.getSku(), cartId, newQuantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            cart.getItems().add(newItem);
            log.debug("Added new SKU {} to cart {} with quantity {}", request.getSku(), cartId, request.getQuantity());
        }

        return cartRepository.save(cart);
    }

    /**
     * Retrieves the current state of a cart.
     * Marked as read-only to optimize performance and handle lazy loading within the transaction.
     *
     * @param cartId The unique identifier of the cart (UUID).
     * @return A DTO representation of the cart.
     */
    @Transactional(readOnly = true)
    public CartDetailsResponse getCart(String cartId) {
        log.debug("Fetching details for cart {}", cartId);

        UUID uuid = UUID.fromString(cartId);
        Cart cart = cartRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

        return mapToCartDetailsResponse(cart);
    }

    /**
     * Helper method to map the Cart entity to the API response DTO.
     */
    private CartDetailsResponse mapToCartDetailsResponse(Cart cart) {
        CartDetailsResponse response = new CartDetailsResponse();
        response.setCartId(cart.getId().toString());

        var itemsDto = cart.getItems().stream()
                .map(item -> {
                    CartItemDto dto = new CartItemDto();
                    dto.setSku(item.getProduct().getSku());
                    dto.setQuantity(item.getQuantity());
                    return dto;
                })
                .toList();
        response.setItems(itemsDto);

        if (cart.getShippingAddress() != null) {
            ShippingAddressDto addrDto = new ShippingAddressDto();
            addrDto.setStreet(cart.getShippingAddress().getStreet());
            addrDto.setCity(cart.getShippingAddress().getCity());
            addrDto.setZoneId(cart.getShippingAddress().getZoneId());
            response.setShippingAddress(addrDto);
        }

        return response;
    }
}