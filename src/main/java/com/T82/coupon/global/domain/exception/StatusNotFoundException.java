package com.T82.coupon.global.domain.exception;

public class StatusNotFoundException extends IllegalArgumentException{
    public StatusNotFoundException() {
        super("존재하지 않는 상태 입니다.");
    }
}
