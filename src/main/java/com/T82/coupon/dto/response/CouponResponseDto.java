package com.T82.coupon.dto.response;

import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.enums.DiscountType;

import java.util.Date;
import java.util.UUID;

public record CouponResponseDto(
        UUID couponId,
        String couponName,
        DiscountType discountType,
        int discountValue,
        Date validEnd,
        Integer minPurchase,
        boolean duplicate,
        Category category

){
    public static CouponResponseDto from(Coupon coupon) {
        return new CouponResponseDto(
                coupon.getCouponId(),
                coupon.getCouponName(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getValidEnd(),
                coupon.getMinPurchase(),
                coupon.getDuplicate(),
                coupon.getCategory()
        );
    }
}
