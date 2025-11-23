package com.test.coupon.util;

import com.test.coupon.dto.Cart;
import com.test.coupon.dto.CartItem;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

@Component
public class ProductWiseStrategy implements CouponStrategy {

    private static final String TYPE = "product-wise";

    @Override
    public boolean supports(String type) {
        return TYPE.equalsIgnoreCase(type);
    }

    /**
     * Checks if the target product exists in the cart.
     */
    @Override
    public boolean isApplicable(Cart cart, Map<String, Object> details) {
        Long targetProductId = getLongFromDetails(details, "product_id");
        
        // Condition: Product X is in the cart
        return cart.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(targetProductId));
    }

    /**
     * Calculates total discount amount for this specific product across all its quantities.
     */
    @Override
    public BigDecimal calculateDiscount(Cart cart, Map<String, Object> details) {
        if (!isApplicable(cart, details)) {
            return BigDecimal.ZERO;
        }

        Long targetProductId = getLongFromDetails(details, "product_id");
        BigDecimal discountPercentage = getBigDecimalFromDetails(details, "discount");

        // Find the item in the cart
        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(targetProductId))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            
            // Calculate whatever the discount is, on the total cost of this item
            return itemTotal.multiply(discountPercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    /**
     * Updates the specific item's discount field and the global cart totals.
     */
    @Override
    public Cart apply(Cart cart, Map<String, Object> details) {
        Long targetProductId = getLongFromDetails(details, "product_id");
        BigDecimal calculatedDiscount = calculateDiscount(cart, details);

        if (calculatedDiscount.compareTo(BigDecimal.ZERO) == 0) {
            return cart;
        }

        // Update the specific item's discount
        for (CartItem item : cart.getItems()) {
            if (item.getProductId().equals(targetProductId)) {
                // Ensure not to overwrite existing discounts if handling stacking later
                item.setTotalDiscount(calculatedDiscount);
            }
        }

        // Recalculate Cart Totals (Final Price)
        updateCartTotals(cart);

        return cart;
    }


    private void updateCartTotals(Cart cart) {
        BigDecimal rawTotal = BigDecimal.ZERO; // Tracks pre-discount total
        BigDecimal finalPrice = BigDecimal.ZERO;
        BigDecimal totalCartDiscount = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            BigDecimal itemCost = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal itemDiscount = item.getTotalDiscount() != null ? item.getTotalDiscount() : BigDecimal.ZERO;

            rawTotal = rawTotal.add(itemCost); // Accumulate the raw total
            finalPrice = finalPrice.add(itemCost.subtract(itemDiscount));
            totalCartDiscount = totalCartDiscount.add(itemDiscount);
        }

        cart.setTotalPrice(rawTotal);
        cart.setFinalPrice(finalPrice);
        cart.setTotalDiscount(totalCartDiscount);
    }

    private Long getLongFromDetails(Map<String, Object> details, String key) {
        Object value = details.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private BigDecimal getBigDecimalFromDetails(Map<String, Object> details, String key) {
        Object value = details.get(key);
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
