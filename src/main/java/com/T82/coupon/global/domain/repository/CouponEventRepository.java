package com.T82.coupon.global.domain.repository;

import com.T82.coupon.global.domain.entity.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long> {
}
