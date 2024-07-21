package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.dto.request.CouponVerifyRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
import com.T82.coupon.dto.response.CouponVerifyResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CouponService {
    void createCoupon(CouponRequestDto req);

    Page<CouponResponseDto> getCouponsByCategory(String category, Pageable pageRequest);

    void giveCouponToUser(String couponId, String userId);

    Page<CouponResponseDto> getValidCoupons(Pageable pageRequest);

    CouponVerifyResponseDto verifyCoupons(CouponVerifyRequestDto req);
}
