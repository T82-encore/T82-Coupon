package com.T82.coupon.global.domain.exception;

public class CategoryNotMatchException extends IllegalArgumentException{
    public CategoryNotMatchException() {
        super("쿠폰의 카테고리가 일치하지 않습니다.");
    }
}