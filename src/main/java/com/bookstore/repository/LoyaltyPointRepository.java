package com.bookstore.repository;

import com.bookstore.model.LoyaltyPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyPointRepository extends JpaRepository<LoyaltyPoint, Long> {

    List<LoyaltyPoint> findByUserId(Long userId);

    @Query("SELECT SUM(lp.points) FROM LoyaltyPoint lp WHERE lp.userId = :userId")
    Integer sumPointsByUserId(@Param("userId") Long userId);
}