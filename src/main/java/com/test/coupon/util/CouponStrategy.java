package com.test.coupon.util;

import com.test.coupon.dto.Cart;

import java.math.BigDecimal;
import java.util.Map;

public interface CouponStrategy {
    boolean supports(String type); // e.g., returns true for "cart-wise"
    boolean isApplicable(Cart cart, Map<String, Object> details);
    BigDecimal calculateDiscount(Cart cart, Map<String, Object> details);
    Cart apply(Cart cart, Map<String, Object> details); // Returns updated cart
}
