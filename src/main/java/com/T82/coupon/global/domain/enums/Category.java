package com.T82.coupon.global.domain.enums;

import com.T82.coupon.global.domain.exception.CategoryNotFoundException;

public enum Category {
    MUSICAL,
    CONCERT,
    SPORTS,
    ALL;
    public static Category from(String str){
        str = str.toUpperCase();
        return switch (str) {
            case "MUSICAL" -> Category.MUSICAL;
            case "CONCERT" -> Category.CONCERT;
            case "SPORTS" -> Category.SPORTS;
            case "ALL" -> Category.ALL;
            default -> throw new CategoryNotFoundException();
        };
    }
}
