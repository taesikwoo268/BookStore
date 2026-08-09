package com.bookstore.repository;

import com.bookstore.model.UserLoyalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLoyaltyRepository extends JpaRepository<UserLoyalty, Long> {

    Optional<UserLoyalty> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE UserLoyalty ul SET ul.totalPoints = ul.totalPoints + :points WHERE ul.userId = :userId")
    int addPoints(@Param("userId") Long userId, @Param("points") Integer points);
}