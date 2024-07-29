package com.T82.coupon.controller;

import com.T82.coupon.global.domain.exception.*;
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

    @ExceptionHandler(StatusNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String statusNotFoundExceptionHandler(StatusNotFoundException e){
        log.error("ERROR : StatusNotFoundException (BE404 - NOTFOUND)", e);
        return e.getMessage();
    }

    @ExceptionHandler(DuplicateCouponException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String DuplicateCouponExceptionHandler(DuplicateCouponException e){
        log.error("ERROR : DuplicateCouponException (BE404 - CONFLICT)", e);
        return e.getMessage();
    }

    @ExceptionHandler(MinPurchaseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String MinPurchaseExceptionHandler(MinPurchaseException e){
        log.error("ERROR : MinPurchaseException (BE404 - CONFLICT)", e);
        return e.getMessage();
    }

    @ExceptionHandler(ExpiredCouponException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String ExpiredCouponExceptionHandler(ExpiredCouponException e){
        log.error("ERROR : ExpiredCouponException (BE404 - CONFLICT)", e);
        return e.getMessage();
    }
}
