package com.test.coupon.util;

import com.test.coupon.dto.Cart;
import com.test.coupon.dto.CartItem;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class BxGyStrategy implements CouponStrategy {

    private static final String TYPE = "bxgy";

    @Override
    public boolean supports(String type) {
        return TYPE.equalsIgnoreCase(type);
    }

    /**
     * Determines if the cart meets the "Buy" requirement.
     */
    @Override
    public boolean isApplicable(Cart cart, Map<String, Object> details) {
        int applicableRepetitions = calculateApplicableRepetitions(cart, details);
        return applicableRepetitions > 0; }

    /**
     * Calculates the total monetary value of the discount.
     */
    @Override
    public BigDecimal calculateDiscount(Cart cart, Map<String, Object> details) {

        // How many times can we apply this deal?
        int repetitions = calculateApplicableRepetitions(cart, details);
        if (repetitions == 0)
            return BigDecimal.ZERO;

        // What are the "Get" items?
        List<Long> getProductIds = extractProductIds(details, "get_products");
        int getQuantityPerRepetition = extractQuantity(details, "get_products");

        // Total items to make free (e.g., 3 repetitions * 1 free item = 3 free items)
        int totalFreeItems = repetitions * getQuantityPerRepetition;

        // Find eligible "Get" items currently in the cart
        List<CartItem> eligibleGetItems = cart.getItems().stream()
                .filter(item -> getProductIds.contains(item.getProductId()))
                .sorted(Comparator.comparing(CartItem::getPrice)) // Optimization: Discount cheapest items first
                .collect(Collectors.toList());

        // Calculate discount sum
        BigDecimal totalDiscount = BigDecimal.ZERO;
        int itemsDiscountedSoFar = 0;

        for (CartItem item : eligibleGetItems) {
            if (itemsDiscountedSoFar >= totalFreeItems)
                break;

            // How many of this specific item can we make free?
            int remainingFreeSlots = totalFreeItems - itemsDiscountedSoFar;
            int countToDiscount = Math.min(item.getQuantity(), remainingFreeSlots);
            BigDecimal itemDiscount = item.getPrice().multiply(BigDecimal.valueOf(countToDiscount));

            totalDiscount = totalDiscount.add(itemDiscount);
            itemsDiscountedSoFar += countToDiscount;
        }

        return totalDiscount;
    }

    /**
     * Applies the discount to the cart object (updating item fields).
     */
    @Override
    public Cart apply(Cart cart, Map<String, Object> details) {

        int repetitions = calculateApplicableRepetitions(cart, details);

        if (repetitions == 0)
            return cart;

        List<Long> getProductIds = extractProductIds(details, "get_products");
        int getQuantityPerRepetition = extractQuantity(details, "get_products");
        int totalFreeItems = repetitions * getQuantityPerRepetition;
        int itemsDiscountedSoFar = 0;

        // Sort eligible items by price (asc) to apply discount to cheapest first
        List<CartItem> eligibleItems = cart.getItems().stream()
                .filter(item -> getProductIds.contains(item.getProductId()))
                .sorted(Comparator.comparing(CartItem::getPrice))
                .toList();

        for (CartItem item : eligibleItems) {

            if (itemsDiscountedSoFar >= totalFreeItems)
                break;

            int remainingFreeSlots = totalFreeItems - itemsDiscountedSoFar;
            int countToDiscount = Math.min(item.getQuantity(), remainingFreeSlots);

            // Update the item's specific discount field
            BigDecimal discountAmount = item.getPrice().multiply(BigDecimal.valueOf(countToDiscount));
            item.setTotalDiscount(discountAmount);
            itemsDiscountedSoFar += countToDiscount;
        }

        // Recalculate Cart Total
        updateCartTotals(cart);

        return cart;
    }


    private int calculateApplicableRepetitions(Cart cart, Map<String, Object> details) {

        // Extract "Buy" constraints
        List<Long> buyProductIds = extractProductIds(details, "buy_products");
        int buyQuantityNeeded = extractQuantity(details, "buy_products");
        int limit = (int) details.getOrDefault("repition_limit", 1);

        // Count how many "Buy" items are in the cart
        int buyItemsInCart = cart.getItems().stream()
                .filter(item -> buyProductIds.contains(item.getProductId()))
                .mapToInt(CartItem::getQuantity)
                .sum();


        if (buyQuantityNeeded == 0)
            return 0;

        // Calculate potential sets (e.g., Buy 6 items / Need 2 = 3 sets)
        int potentialSets = buyItemsInCart / buyQuantityNeeded;

        // Cap by repetition limit
        return Math.min(potentialSets, limit);
    }

    private void updateCartTotals(Cart cart) {

        BigDecimal rawTotal = BigDecimal.ZERO; // Tracks pre-discount total
        BigDecimal finalPrice = BigDecimal.ZERO;
        BigDecimal totalCartDiscount = BigDecimal.ZERO;

        for(CartItem item : cart.getItems()) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal itemDiscount = item.getTotalDiscount() != null ? item.getTotalDiscount() : BigDecimal.ZERO;

            rawTotal = rawTotal.add(itemTotal); // Accumulate the raw total
            finalPrice = finalPrice.add(itemTotal.subtract(itemDiscount));

            totalCartDiscount = totalCartDiscount.add(itemDiscount);
        }

        cart.setTotalPrice(rawTotal);
        cart.setFinalPrice(finalPrice);
        cart.setTotalDiscount(totalCartDiscount);
    }


    @SuppressWarnings("unchecked")
    private List<Long> extractProductIds(Map<String, Object> details, String key) {
        List<Map<String, Object>> products = (List<Map<String, Object>>) details.get(key);
        return products.stream()
                .map(p -> ((Number) p.get("product_id")).longValue())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private int extractQuantity(Map<String, Object> details, String key) {
        List<Map<String, Object>> products = (List<Map<String, Object>>) details.get(key);

        // Assumption: The "quantity" in the definition array is the TOTAL required for that pool
        // For simplicity, we take the first quantity found
        return products.stream()
                .mapToInt(p -> ((Number) p.get("quantity")).intValue())
                .findFirst()
                .orElse(1);
    }

}