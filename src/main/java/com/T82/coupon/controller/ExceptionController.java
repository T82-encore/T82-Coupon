package com.T82.coupon.controller;

import com.T82.coupon.global.domain.exception.CategoryNotFoundException;
import com.T82.coupon.global.domain.exception.CouponNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(CouponNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String couponNotFoundExceptionHandler(CouponNotFoundException e){
        return e.getMessage();
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String categoryNotFoundExceptionHandler(CategoryNotFoundException e){
        return e.getMessage();
    }
}
