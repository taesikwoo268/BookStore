package com.bookstore.repository;

import com.bookstore.model.Order;
import com.bookstore.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Override
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.orderItems oi " +
            "JOIN FETCH oi.book b " +
            "JOIN FETCH o.user u " +
            "WHERE o.id = :id")
    Optional<Order> findById(@Param("id")Long id);

    List<Order> findByStatusAndOrderDateBefore(OrderStatus status, LocalDateTime orderDate);
}