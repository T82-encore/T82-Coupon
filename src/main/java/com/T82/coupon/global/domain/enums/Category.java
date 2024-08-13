package com.T82.coupon.global.domain.enums;

public enum Category {
    MUSICAL,
    CONCERT,
    SPORTS,
    PEDOMETER,
    ALL;
    public static Category from(String str){
        str = str.toUpperCase();
        return switch (str) {
            case "MUSICAL" -> Category.MUSICAL;
            case "CONCERT" -> Category.CONCERT;
            case "SPORTS" -> Category.SPORTS;
            case "ALL" -> Category.ALL;
            case "PEDOMETER" -> Category.PEDOMETER;
            default -> throw new IllegalArgumentException();
        };
    }
}
