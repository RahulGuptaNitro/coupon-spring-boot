package com.test.coupon.service;

import com.test.coupon.dto.Cart;
import com.test.coupon.entity.Coupon;
import com.test.coupon.exception.CouponException;

import java.util.List;
import java.util.Map;

public interface CouponService {

    // CRUD Operations
    Coupon createCoupon(Coupon coupon);

    List<Coupon> getAllCoupons();

    Coupon getCouponById(Long id) throws CouponException;

    Coupon updateCoupon(Long id, Coupon coupon) throws CouponException;

    Coupon deleteCoupon(Long id) throws CouponException;


    // Logic Operations
    Map<String, Object> getApplicableCoupons(Cart cart) throws CouponException;

    Cart applyCoupon(Long id, Cart cart) throws CouponException;

}