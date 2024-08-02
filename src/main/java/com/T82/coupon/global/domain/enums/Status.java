package com.T82.coupon.global.domain.enums;


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
            default -> throw new IllegalArgumentException();
        };
    }

    public boolean validateIsExpired() {
        return !this.equals(Status.UNUSED);
    }
}
