package com.T82.coupon.controller;

import com.T82.coupon.global.domain.exception.CategoryNotFoundException;
import com.T82.coupon.global.domain.exception.CouponNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(CouponNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String couponNotFoundExceptionHandler(CouponNotFoundException e){
        log.error("ERROR : CouponNotFoundException (BE404 - NOTFOUND)", e);
        return e.getMessage();
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String categoryNotFoundExceptionHandler(CategoryNotFoundException e){
        log.error("ERROR : CouponNotFoundException (BE404 - NOTFOUND)", e);
        return e.getMessage();
    }
}
