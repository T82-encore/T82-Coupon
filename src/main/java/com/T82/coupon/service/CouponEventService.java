package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponEventRequestDto;
import com.T82.coupon.global.domain.dto.UserDto;

public interface CouponEventService {
    void createCouponEvent(CouponEventRequestDto req);
    void issueCoupon(String userId, String couponId);
}
