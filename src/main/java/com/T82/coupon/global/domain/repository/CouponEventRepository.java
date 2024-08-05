package com.T82.coupon.global.domain.repository;

import com.T82.coupon.global.domain.entity.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long> {
    Optional<CouponEvent> findByCoupon_CouponId(UUID couponId);
}
