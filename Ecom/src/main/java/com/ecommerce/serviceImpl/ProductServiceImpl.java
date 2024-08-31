package com.ecommerce.serviceImpl;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ecommerce.config.CustomeUserDetails;
import com.ecommerce.config.UserDetailServiceImp;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.User;
import com.ecommerce.exceptions.CategoryNotFoundException;
import com.ecommerce.service.CategoryService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.dao.ProductRepository;

import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {
	
	   @Autowired
	   private ProductRepository productRepository;

	   @Autowired
	   private CategoryService categoryService;

	   @Autowired
	   private UserDetailServiceImp userDetailServiceImp;

	   @Autowired
	   private CartServiceImpl cartService;



	   @Override
	    public Product createProduct(int categoryId, Product product)  {
		   Optional<Category> optionalCategory = categoryService.getCategoryById(categoryId);

		   Category category = optionalCategory.orElseThrow(() ->
				    new CategoryNotFoundException("Category not found with ID: " + categoryId));

		   product.setCategory(category);
		   return productRepository.save(product);
	    }

	    @Override
	    public Product updateProduct(int id, Product product) {
			Product product1 = productRepository.findById(id).get();
			product.setId(id);
			if(product.getImage()==null){
				product.setImage(product1.getImage());
			}
	        return productRepository.save(product);
	    }

	    @Override
		@Transactional
	    public void deleteProduct(int id) {
		    cartService.deleteByProductId(id);
	        productRepository.deleteById(id);

	    }

	    @Override
	    public Optional<ProductDTO> getProductById(int id) {
			Optional<Product> productOptional = productRepository.findById(id);
			return productOptional.map(ProductDTO::new);
	    }

	    @Override
	    public List<ProductDTO> getAllProducts() {
			List<Product> productList = productRepository.findAll();
			return productList.stream()
					.map(ProductDTO::new)
					.collect(Collectors.toList());
		}

	public List<ProductDTO> getAllProducts(Principal principal) {
		CustomeUserDetails customUserDetails = (CustomeUserDetails) this.userDetailServiceImp.loadUserByUsername(principal.getName());
		User user = customUserDetails.getUser();

		List<Cart> carts = cartService.getCartItemsByUserId(user.getId());

		Map<Integer, Integer> productQuantityMap = carts.stream()
				.collect(Collectors.toMap(Cart::getProductId, Cart::getQuantity));

		List<Product> productList = productRepository.findAll();

		List<ProductDTO> productDTOS = productList.stream()
				.map(product -> {
					ProductDTO productDTO = new ProductDTO(product);
					if (productQuantityMap.containsKey(product.getId())) {
						productDTO.setQuantity(productQuantityMap.get(product.getId()));
					}
					return productDTO;
				})
				.collect(Collectors.toList());

		return productDTOS;
	}


}
