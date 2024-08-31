package com.ecommerce.controller;

import com.ecommerce.dto.CartDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("cart")
@CrossOrigin
public class CartController {

    @Autowired
    private CartService cartService;

    // Add a product to the cart
    @PostMapping("/addToCart")
    public ResponseEntity<Cart> addToCart(

            @RequestParam int productId, Principal principal) {
        Cart cartItem = cartService.addToCart(principal, productId);
        return ResponseEntity.ok(cartItem);
    }

    @GetMapping("/total-quantity")
    public Integer getTotalQuantityByUserId(Principal principal) {
        return cartService.getTotalQuantityForUser(principal);
    }
    @PostMapping("/decrementProductQuantityInCart")
    public ResponseEntity<Cart> decrementProductQuantityInCart(
            @RequestParam int productId, Principal principal) {
        Cart cartItem = cartService.decrementProductQuantityInCart(principal, productId);
        return ResponseEntity.ok(cartItem);
    }

    // Get all cart items for a specific user
    @GetMapping("/{userId}")
    public ResponseEntity<List<Cart>> getCartItemsByUserId(@PathVariable int userId) {
        List<Cart> cartItems = cartService.getCartItemsByUserId(userId);
        return ResponseEntity.ok(cartItems);
    }

    //get all carts
    @GetMapping("/")
    public ResponseEntity<List<CartDto>> getAllCartDtos(Principal principal) {
        List<CartDto> cartItems = cartService.getCartItemsByUserId(principal);
        return ResponseEntity.ok(cartItems);
    }

    // Remove a specific item from the cart
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.noContent().build();
    }

    // Clear the cart for a specific user
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart( @RequestParam int productId, Principal principal) {
        cartService.clearCart( principal,  productId);
        return ResponseEntity.noContent().build();
    }
}

