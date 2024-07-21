package com.T82.coupon.dto.response;

public record CouponVerifyResponseDto (String status){
    public static CouponVerifyResponseDto from(String res){
        return new CouponVerifyResponseDto(res);
    }
}
