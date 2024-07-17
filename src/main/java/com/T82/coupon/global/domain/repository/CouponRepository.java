package com.T82.coupon.global.domain.repository;

import com.T82.coupon.global.domain.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
