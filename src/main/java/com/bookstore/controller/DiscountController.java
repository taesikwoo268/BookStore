package com.bookstore.controller;

import com.bookstore.strategy.DiscountContext;
import com.bookstore.strategy.discount.*;
import com.bookstore.dto.response.ApiResponse;
import com.bookstore.service.DiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/discounts")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class DiscountController {

    private final DiscountContext discountContext;
    private final DiscountService discountService;

    @GetMapping("/strategies")
    public ApiResponse<List<DiscountStrategy>> getStrategies() {
        return ApiResponse.success(discountContext.getStrategies());
    }

    @PostMapping("/percentage")
    public ApiResponse<String> addPercentageDiscount(@RequestParam BigDecimal percentage) {
        discountContext.addStrategy(new PercentageDiscount(percentage));
        return ApiResponse.success("Added percentage discount: " + percentage + "%");
    }

    @PostMapping("/fixed")
    public ApiResponse<String> addFixedDiscount(@RequestParam BigDecimal amount) {
        discountContext.addStrategy(new FixedAmountDiscount(amount));
        return ApiResponse.success("Added fixed discount: $" + amount);
    }

    @PostMapping("/buy-x-get-y")
    public ApiResponse<String> addBuyXGetYDiscount(
            @RequestParam int x,
            @RequestParam int y,
            @RequestParam BigDecimal discountPercentage) {
        discountContext.addStrategy(new BuyXGetYDiscount(x, y, discountPercentage));
        return ApiResponse.success("Added Buy " + x + " Get " + y + " discount");
    }

    @PostMapping("/init")
    public ApiResponse<String> initDiscounts() {
        discountService.initDiscounts();
        return ApiResponse.success("Discounts initialized");
    }
}