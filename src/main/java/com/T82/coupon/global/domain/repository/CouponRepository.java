package com.T82.coupon.global.domain.repository;

import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    @Query("select c from Coupon c where c.category=:category")
    Page<Coupon> findAllByCategory(@Param("category") Category category, Pageable pageRequest);
}
