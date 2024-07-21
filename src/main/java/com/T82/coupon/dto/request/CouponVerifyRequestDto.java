package com.T82.coupon.dto.request;

import com.T82.coupon.global.domain.enums.Category;

import java.util.List;


public record CouponVerifyRequestDto (String userId, Integer amount,String category,List<CouponUsage> coupons){
    public record CouponUsage (String couponId){
    }

}
