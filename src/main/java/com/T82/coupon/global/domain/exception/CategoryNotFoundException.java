package com.T82.coupon.global.domain.exception;

public class CategoryNotFoundException extends IllegalArgumentException{
    public CategoryNotFoundException() {
        super("Not Fount Category");
    }
}
