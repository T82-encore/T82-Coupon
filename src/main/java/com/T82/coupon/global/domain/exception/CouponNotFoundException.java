package com.T82.coupon.global.domain.exception;

public class CouponNotFoundException extends IllegalArgumentException{
    public CouponNotFoundException() {
        super("Not Fount Coupon");
    }
}
