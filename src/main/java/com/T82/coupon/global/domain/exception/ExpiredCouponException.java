package com.T82.coupon.global.domain.exception;

public class ExpiredCouponException extends IllegalArgumentException{
    public ExpiredCouponException() {
        super("만료된 쿠폰입니다.");
    }
}
