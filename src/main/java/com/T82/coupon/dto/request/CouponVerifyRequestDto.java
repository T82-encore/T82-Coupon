package com.T82.coupon.dto.request;

import java.util.List;


public record CouponVerifyRequestDto (String userId, List<CouponUsage> items){
    public record CouponUsage (List<String> couponIds, int beforeAmount){
    }
}
