package com.bookstore.controller;

import com.bookstore.dto.response.ApiResponse;
import com.bookstore.model.UserLoyalty;
import com.bookstore.repository.UserLoyaltyRepository;
import com.bookstore.service.LoyaltyPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
@Slf4j
public class LoyaltyController {

    private final LoyaltyPointService loyaltyPointService;
    private final UserLoyaltyRepository userLoyaltyRepository;

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ApiResponse<UserLoyalty> getUserLoyalty(@PathVariable Long userId) {
        UserLoyalty userLoyalty = userLoyaltyRepository.findByUserId(userId)
                .orElse(UserLoyalty.builder()
                        .userId(userId)
                        .totalPoints(0)
                        .tier("BRONZE")
                        .build());
        return ApiResponse.success(userLoyalty);
    }

    @GetMapping("/{userId}/total")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ApiResponse<Integer> getTotalPoints(@PathVariable Long userId) {
        Integer points = loyaltyPointService.getTotalPoints(userId);
        return ApiResponse.success(points);
    }

    @GetMapping("/{userId}/tier")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ApiResponse<String> getUserTier(@PathVariable Long userId) {
        String tier = loyaltyPointService.getUserTier(userId);
        return ApiResponse.success(tier);
    }
}