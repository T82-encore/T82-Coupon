package com.T82.coupon.global.domain.exception;

public class CategoryNotFoundException extends IllegalArgumentException{
    public CategoryNotFoundException() {
        super("존재하지 않는 카테고리 입니다.");
    }
}
