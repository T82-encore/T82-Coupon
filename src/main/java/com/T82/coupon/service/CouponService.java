package com.T82.coupon.service;

import com.T82.coupon.dto.request.CreateCouponRequestDto;

public interface CouponService {
    void createCoupon(CreateCouponRequestDto req);
}
