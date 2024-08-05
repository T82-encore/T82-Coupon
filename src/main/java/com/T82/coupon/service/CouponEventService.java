package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponEventRequestDto;

import java.util.UUID;

public interface CouponEventService {
    void createCouponEvent(CouponEventRequestDto req);
    void issueCoupon(String userId, String couponId);
}
