package com.T82.coupon.global.domain.exception;
public class DuplicateCouponException extends IllegalArgumentException{
    public DuplicateCouponException() {
        super("중복 불가능한 쿠폰이 포함되어있습니다.");
    }
}
