package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponEventRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;

import java.util.List;
import java.util.UUID;

public interface CouponEventService {
    void createCouponEvent(CouponEventRequestDto req);
    void issueCoupon(String userId, String couponId);
    List<CouponResponseDto> getEventCoupons();
}
