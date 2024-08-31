package com.ecommerce.serviceImpl;

import com.ecommerce.config.CustomeUserDetails;
import com.ecommerce.config.UserDetailServiceImp;
import com.ecommerce.dao.CartRepository;
import com.ecommerce.dao.ProductRepository;
import com.ecommerce.dto.CartDto;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Cart;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.service.CartService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.ProviderNotFoundException;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    UserDetailServiceImp userDetailServiceImp;


    @Override
    public Cart addToCart(Principal principal, int productId) {
        CustomeUserDetails customUserDetails = (CustomeUserDetails) this.userDetailServiceImp.loadUserByUsername(principal.getName());
        User user = customUserDetails.getUser();

        Cart existingCartItem = cartRepository.findByUserIdAndProductId(user.getId(), productId);

        if (existingCartItem != null) {
            int newQuantity = existingCartItem.getQuantity() + 1;
            float pricePerUnit = getPriceForProduct(productId);
            float newTotalPrice = pricePerUnit * newQuantity;

            existingCartItem.setQuantity(newQuantity);
            existingCartItem.setTotalPrice(newTotalPrice);

            return cartRepository.save(existingCartItem);
        } else {
            float price = getPriceForProduct(productId);

            Cart newCartItem = new Cart(user.getId(), productId, 1, price);
            return cartRepository.save(newCartItem);
        }
    }


    @Override
    public Cart decrementProductQuantityInCart(Principal principal, int productId) {
        CustomeUserDetails customUserDetails = (CustomeUserDetails) this.userDetailServiceImp.loadUserByUsername(principal.getName());
        User user = customUserDetails.getUser();

        Cart existingCartItem = cartRepository.findByUserIdAndProductId(user.getId(), productId);

        if (existingCartItem != null) {
            int newQuantity = existingCartItem.getQuantity() - 1;
            float pricePerUnit = getPriceForProduct(productId);

            if (newQuantity > 0) {
                float newTotalPrice = pricePerUnit * newQuantity;
                existingCartItem.setQuantity(newQuantity);
                existingCartItem.setTotalPrice(newTotalPrice);
                return cartRepository.save(existingCartItem);
            } else if (existingCartItem.getQuantity() == 1) {
                cartRepository.delete(existingCartItem);
                return null;
            }
        }
        return null;
    }
    @Override
    public Integer getTotalQuantityForUser(Principal principal) {
        CustomeUserDetails customUserDetails = (CustomeUserDetails) this.userDetailServiceImp.loadUserByUsername(principal.getName());
        User user = customUserDetails.getUser();

        return cartRepository.getTotalQuantityByUserId(user.getId());
    }

    @Override
    public List<Cart> getCartItemsByUserId(int userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    public List<CartDto> getCartItemsByUserId(Principal principal) {
        CustomeUserDetails customUserDetails = (CustomeUserDetails) this.userDetailServiceImp.loadUserByUsername(principal.getName());
        User user = customUserDetails.getUser();

        List<Cart> carts = getCartItemsByUserId(user.getId());

        List<Integer> productIds = carts.stream()
                .map(Cart::getProductId)
                .collect(Collectors.toList());

        List<Product> products = productRepository.findAllById(productIds);

        Map<Integer, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        List<CartDto> cartDtos = carts.stream().map(cart -> {
            CartDto cartDto = new CartDto();
            cartDto.setId(cart.getId());
            cartDto.setProductId(cart.getProductId());
            cartDto.setUserId(cart.getUserId());
            cartDto.setQuantity(cart.getQuantity());

            Product product = productMap.get(cart.getProductId());
            if (product != null) {
                cartDto.setName(product.getName());
                cartDto.setBrand(product.getBrand());
                cartDto.setDescription(product.getDescription());
                cartDto.setPrice(product.getPrice());
                cartDto.setDiscount(product.getDiscount());
                if (product.getImage() != null) {
                    cartDto.setImage(Base64.getEncoder().encodeToString(product.getImage()));
                }
                cartDto.setCategory(product.getCategory());

                float discountedPrice = product.getPrice() * (1 - (product.getDiscount() / 100));
                float totalAmount = discountedPrice * cart.getQuantity();
                cartDto.setTotalAmount(totalAmount);
            }

            return cartDto;
        }).collect(Collectors.toList());

        return cartDtos;
    }



    @Override
    public void removeCartItem(long cartItemId) {
        cartRepository.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Principal principal, int productId) {
        CustomeUserDetails customUserDetails = (CustomeUserDetails) this.userDetailServiceImp.loadUserByUsername(principal.getName());
        User user = customUserDetails.getUser();
        cartRepository.deleteByUserIdAndProductId(user.getId(),productId);
    }

    public float getPriceForProduct(int productId) {
        Optional<Product> productOptional = productRepository.findById(productId);

        Float price = productOptional.map(Product::getPrice)
                .orElseThrow(() -> new ProviderNotFoundException("Product not found"));

        return price * (1 - (productOptional.get().getDiscount() / 100));
    }

    public void  deleteByProductId(int productId){
        cartRepository.deleteByProductId(productId);
    }

}
