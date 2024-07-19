package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponService {
    void createCoupon(CouponRequestDto req);

    Page<CouponResponseDto> getCouponsByCategory(String category, Pageable pageRequest);

    void giveCouponToUser(String couponId, String userId);

    Page<CouponResponseDto> getValidCoupons(Pageable pageRequest);
}
