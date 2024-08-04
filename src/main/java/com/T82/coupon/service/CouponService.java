package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponEventRequestDto;
import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.dto.request.CouponVerifyRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
import com.T82.coupon.dto.response.CouponVerifyResponseDto;
import com.T82.coupon.global.domain.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CouponService {
    void createCoupon(CouponRequestDto req);

    Page<CouponResponseDto> getCouponsByCategory(String category, Pageable pageRequest);

    void giveCouponToUser(String couponId, UserDto userDto);

    Page<CouponResponseDto> getValidCoupons(Pageable pageRequest,UserDto userDto);

    CouponVerifyResponseDto verifyCoupons(CouponVerifyRequestDto req);


}
