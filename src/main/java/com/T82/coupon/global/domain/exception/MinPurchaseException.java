package com.T82.coupon.global.domain.exception;

public class MinPurchaseException extends IllegalArgumentException{
    public MinPurchaseException() {
        super("쿠폰을 사용할 수 있는 최소금액을 만족하지 않습니다.");
    }
}