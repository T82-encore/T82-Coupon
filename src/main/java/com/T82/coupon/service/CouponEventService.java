package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponEventRequestDto;
import com.T82.coupon.global.domain.dto.IssueCouponDto;
import com.T82.coupon.global.domain.dto.UserDto;
import com.T82.coupon.global.domain.entity.CouponEvent;

import java.util.UUID;

public interface CouponEventService {
    void createCouponEvent(CouponEventRequestDto req);
    void issueCoupon(String userId, String couponId);
}
