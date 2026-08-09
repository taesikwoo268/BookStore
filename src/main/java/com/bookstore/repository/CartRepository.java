package com.bookstore.repository;

import com.bookstore.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
//    @Query("SELECT c FROM Cart c " +
//            "JOIN FETCH c.cartItems ci " +
//            "JOIN FETCH ci.book b " +
//            "WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void clearCartItems(@Param("cartId") Long cartId);
}
