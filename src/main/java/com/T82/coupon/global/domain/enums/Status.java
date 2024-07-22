package com.T82.coupon.global.domain.enums;

import com.T82.coupon.global.domain.exception.StatusNotFoundException;

public enum Status {
    UNUSED,
    USED,
    EXPIRED;
    public static Status from(String str){
        str = str.toUpperCase();
        return switch (str) {
            case "UNUSED" -> Status.UNUSED;
            case "USED" -> Status.USED;
            case "EXPIRED" -> Status.EXPIRED;
            default -> throw new StatusNotFoundException();
        };
    }
}
