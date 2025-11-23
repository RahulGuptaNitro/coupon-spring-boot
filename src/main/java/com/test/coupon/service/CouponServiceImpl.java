package com.test.coupon.service;

import com.test.coupon.dto.Cart;
import com.test.coupon.entity.Coupon;
import com.test.coupon.exception.CouponException;
import com.test.coupon.repository.CouponRepository;
import com.test.coupon.util.CouponStrategy;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service(value = "CouponService")
@Transactional
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final List<CouponStrategy> strategies;

    public CouponServiceImpl(CouponRepository repo, List<CouponStrategy> strategies) {
        this.couponRepository = repo;
        this.strategies = strategies;
    }


    @Override
    public Coupon createCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public Coupon getCouponById(Long id) throws CouponException {
        return couponRepository.findById(id)
                .orElseThrow(() -> new CouponException("Coupon not found with ID: " + id));
    }

    @Override
    public Coupon updateCoupon(Long id, Coupon coupon) throws CouponException {

        // Check if coupon exists before updating
        Coupon existingCoupon = getCouponById(id);

        // Update fields
        existingCoupon.setType(coupon.getType());
        existingCoupon.setDetails(coupon.getDetails());
        existingCoupon.setActive(coupon.isActive());

        return couponRepository.save(existingCoupon);
    }

    @Override
    public Coupon deleteCoupon(Long id) throws CouponException {

        // Check if coupon exists before deleting
        Coupon couponToDelete = getCouponById(id);

        // Delete the coupon
        couponRepository.deleteById(id);

        return couponToDelete;
    }


    /**
     * Finds and applies a specific coupon to the cart.
     */
    @Override
    public Cart applyCoupon(Long id, Cart cart) throws CouponException {
        Coupon coupon = getCouponById(id);

        // Find the matching strategy
        CouponStrategy strategy = strategies.stream()
                .filter(s -> s.supports(coupon.getType()))
                .findFirst()
                .orElseThrow(() -> new CouponException("Unknown coupon type: " + coupon.getType()));

        // Check if applicable and apply
        if (strategy.isApplicable(cart, coupon.getDetails())) {
            return strategy.apply(cart, coupon.getDetails());
        }

        // If not applicable, return the cart unmodified
        return cart;
    }

    /**
     * Calculates potential discounts for all applicable coupons.
     */
    @Override
    public Map<String, Object> getApplicableCoupons(Cart cart) {
        List<Coupon> allCoupons = couponRepository.findAll();
        List<Object> applicableList = new ArrayList<>();

        for (Coupon coupon : allCoupons) {
            // Find the matching strategy
            CouponStrategy strategy = strategies.stream()
                    .filter(s -> s.supports(coupon.getType()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unknown coupon type"));

            if (strategy.isApplicable(cart, coupon.getDetails())) {
                BigDecimal discount = strategy.calculateDiscount(cart, coupon.getDetails());
                applicableList.add(Map.of(
                        "coupon_id", coupon.getId(),
                        "type", coupon.getType(),
                        "discount", discount
                ));
            }
        }

        return Map.of("applicable_coupons", applicableList);
    }
}