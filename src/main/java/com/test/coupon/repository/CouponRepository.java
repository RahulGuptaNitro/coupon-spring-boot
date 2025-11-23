package com.test.coupon.repository;

import com.test.coupon.entity.Coupon;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CouponRepository extends CrudRepository<Coupon, Long> {

    public List<Coupon> findAll();

}
