package com.T82.coupon.dto.response;

public record CouponVerifyResponseDto (String result){
    public static CouponVerifyResponseDto from(String res){
        return new CouponVerifyResponseDto(res);
    }
}
