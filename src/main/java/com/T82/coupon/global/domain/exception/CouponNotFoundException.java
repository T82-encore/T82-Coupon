package com.T82.coupon.global.domain.exception;

public class CouponNotFoundException extends IllegalArgumentException{
    public CouponNotFoundException() {
        super("존재하지 않는 쿠폰입니다.");
    }
}
