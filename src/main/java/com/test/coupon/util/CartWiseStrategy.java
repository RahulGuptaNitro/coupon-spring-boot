package com.test.coupon.util;

import com.test.coupon.dto.Cart;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
public class CartWiseStrategy implements CouponStrategy {

    private static final String TYPE = "cart-wise";

    @Override
    public boolean supports(String type) {
        return TYPE.equalsIgnoreCase(type);
    }

    /**
     * Checks if the cart total > threshold.
     */
    @Override
    public boolean isApplicable(Cart cart, Map<String, Object> details) {
        BigDecimal threshold = getBigDecimalFromDetails(details, "threshold");
        BigDecimal cartTotal = calculateCartTotal(cart);

        // Condition: Cart total > Threshold
        return cartTotal.compareTo(threshold) > 0;
    }

    /**
     * Calculates the raw discount amount (e.g., 10% of 500 = 50).
     */
    @Override
    public BigDecimal calculateDiscount(Cart cart, Map<String, Object> details) {
        if (!isApplicable(cart, details)) {
            return BigDecimal.ZERO;
        }

        BigDecimal cartTotal = calculateCartTotal(cart);
        BigDecimal discountPercentage = getBigDecimalFromDetails(details, "discount");

        // Total * (Discount / 100)
        return cartTotal.multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Applies the coupon and returns the updated cart structure.
     * This matches the "Response" structure required
     */
    @Override
    public Cart apply(Cart cart, Map<String, Object> details) {
        BigDecimal discountAmount = calculateDiscount(cart, details);
        BigDecimal currentTotal = calculateCartTotal(cart);

        // If no discount is applicable, return cart as is (with 0 discount)
        if (discountAmount.compareTo(BigDecimal.ZERO) == 0) {
            cart.setTotalDiscount(BigDecimal.ZERO);
            cart.setFinalPrice(currentTotal);
            return cart;
        }

        // Update Cart-level fields
        cart.setTotalDiscount(discountAmount);
        cart.setFinalPrice(currentTotal.subtract(discountAmount));

        return cart;
    }


    private BigDecimal calculateCartTotal(Cart cart) {
        // Sum of (Price * Quantity) for all items
        return cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Safe extraction of BigDecimal from the loose Map<String, Object> details.
     * Handles Integer vs Double issues from JSON parsing.
     */
    private BigDecimal getBigDecimalFromDetails(Map<String, Object> details, String key) {
        Object value = details.get(key);
        if (value == null) return BigDecimal.ZERO;

        if (value instanceof Integer) {
            return BigDecimal.valueOf((Integer) value);
        } else if (value instanceof Double) {
            return BigDecimal.valueOf((Double) value);
        } else if (value instanceof String) {
            return new BigDecimal((String) value);
        }
        return BigDecimal.ZERO;
    }
}
