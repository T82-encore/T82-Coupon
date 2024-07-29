package com.T82.coupon.global.domain.repository;

import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.entity.CouponBox;
import com.T82.coupon.global.domain.entity.CouponBoxId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CouponBoxRepository extends JpaRepository<CouponBox, CouponBoxId> {
    @Query("select c from Coupon c join fetch c.couponBoxes cb where cb.id.userId = :userId and c.validEnd > current_date and cb.status = com.T82.coupon.global.domain.enums.Status.UNUSED")
            Page<Coupon> findAllByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("select cb from CouponBox cb where cb.id.userId = :userId and cb.id.coupon.couponId=:couponId")
    Optional<CouponBox> findByCouponIdAndUserId(@Param("couponId") UUID couponId, @Param("userId") String userId);
}
