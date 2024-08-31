package com.ecommerce.dao;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserId(int userId);

    Cart  findByUserIdAndProductId(int userId,int productId);

    void  deleteByUserIdAndProductId(int userId,int productId);
    @Query("SELECT SUM(c.quantity) FROM Cart c WHERE c.userId = :userId")
    Integer getTotalQuantityByUserId(@Param("userId") int userId);

    void  deleteByProductId(int productId);
}
