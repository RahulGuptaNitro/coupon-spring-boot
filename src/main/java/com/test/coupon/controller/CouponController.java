package com.test.coupon.controller;

import com.test.coupon.dto.Cart;
import com.test.coupon.entity.Coupon;
import com.test.coupon.exception.CouponException;
import com.test.coupon.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/monkcommerce")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }


    /**
     * Create a new coupon.
     * Requirement: POST /coupons
     */
    @PostMapping("/coupons")
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {
        Coupon created = couponService.createCoupon(coupon);
        return ResponseEntity.ok(created);
    }

    /**
     * Retrieve all coupons.
     * Requirement: GET /coupons
     */
    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    /**
     * Retrieve a specific coupon by its ID.
     * Requirement: GET /coupons/{id}
     */
    @GetMapping("/coupons/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable Long id) throws CouponException {
        return ResponseEntity.ok(couponService.getCouponById(id));
    }

    /**
     * Update a specific coupon by its ID.
     * Requirement: PUT /coupons/{id}
     */
    @PutMapping("/coupons/{id}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable Long id, @RequestBody Coupon coupon) throws CouponException {
        return ResponseEntity.ok(couponService.updateCoupon(id, coupon));
    }

    /**
     * Delete a specific coupon by its ID.
     * Requirement: DELETE /coupons/{id}
     */
    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<Coupon> deleteCoupon(@PathVariable Long id) throws CouponException {
        return ResponseEntity.ok(couponService.deleteCoupon(id));
    }


    /**
     * Fetch all applicable coupons for a given cart and calculate potential discounts.
     * Requirement: POST /applicable-coupons
     */
    @PostMapping("/applicable-coupons")
    public ResponseEntity<Map<String, Object>> getApplicableCoupons(@RequestBody CartWrapper cartWrapper) throws CouponException {

        Cart cart = cartWrapper.getCart();
        Map<String, Object> response = couponService.getApplicableCoupons(cart);

        return ResponseEntity.ok(response);
    }

    /**
     * Apply a specific coupon to the cart and return the updated cart with discounted prices.
     * Requirement: POST /apply-coupon/{id}
     */
    @PostMapping("/apply-coupon/{id}")
    public ResponseEntity<Map<String, Object>> applyCoupon(@PathVariable Long id, @RequestBody CartWrapper cartWrapper) throws CouponException {
        Cart cart = cartWrapper.getCart();
        Cart updatedCart = couponService.applyCoupon(id, cart);

        // Wrap response to match requirement: {"updated_cart": {...}}
        return ResponseEntity.ok(Map.of("updated_cart", updatedCart));
    }


    // --- Helper DTO for Request Bodies ---

    // Needed because the request JSON is wrapped in a "cart" object
    public static class CartWrapper {

        private Cart cart;

        public Cart getCart() {
            return cart;
        }

        public void setCart(Cart cart) {
            this.cart = cart;
        }

    }

}