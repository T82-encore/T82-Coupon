package com.T82.coupon.global.domain.repository;

import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    @Query("select c from Coupon c where c.category=:category or c.category=com.T82.coupon.global.domain.enums.Category.PEDOMETER and c.validEnd > current_date")
    List<Coupon> findAllByCategory(@Param("category") Category category);

    @Query("select distinct ce.coupon from CouponEvent ce where  ce.eventStartTime < current_date")
    List<Coupon> findEventCoupons();

}
