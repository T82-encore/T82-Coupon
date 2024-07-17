package com.T82.coupon.dto.request;

import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.enums.DiscountType;

import java.util.Date;
import java.util.UUID;

public record CreateCouponRequestDto(
        String couponName,
        DiscountType discountType,
        Integer discountValue,
        Date validEnd,
        Integer minPurchase,
        Boolean duplicate,
        Category category

) {
    public Coupon toEntity(CreateCouponRequestDto req){
        return Coupon.builder()
                .couponId(UUID.randomUUID())
                .couponName(req.couponName)
                .discountType(req.discountType)
                .discountValue(req.discountValue)
                .validEnd(req.validEnd)
                .minPurchase(req.minPurchase)
                .duplicate(req.duplicate)
                .category(req.category)
                .build();
    }
}
