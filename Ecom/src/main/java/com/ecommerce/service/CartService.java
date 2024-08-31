package com.ecommerce.service;

import com.ecommerce.dto.CartDto;
import com.ecommerce.entity.Cart;

import java.security.Principal;
import java.util.List;

public interface CartService {
    Cart addToCart(Principal principal, int productId);
    Cart decrementProductQuantityInCart(Principal principal, int productId);
    Integer getTotalQuantityForUser(Principal principal);

    List<Cart> getCartItemsByUserId(int userId);

    void removeCartItem(long cartItemId);

    void clearCart(Principal principal, int productId);

    List<CartDto> getCartItemsByUserId(Principal principal);

    void  deleteByProductId(int productId);

}
