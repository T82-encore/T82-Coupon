package com.T82.coupon.global.domain.repository;

import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.entity.CouponBox;
import com.T82.coupon.global.domain.entity.CouponBoxId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponBoxRepository extends JpaRepository<CouponBox, CouponBoxId> {
    @Query("select c from Coupon c join fetch c.couponBoxes cb where cb.id.userId = :userId and c.validEnd > current_date")
            Page<Coupon> findAllByUserId(@Param("userId") String userId, Pageable pageable);

}
