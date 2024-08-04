package com.T82.coupon.dto.request;

import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.entity.CouponEvent;
import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.enums.DiscountType;

import java.util.Date;
import java.util.UUID;

public record CouponEventRequestDto(
        String couponName,
        String discountType,
        Integer discountValue,
        Date validEnd,
        Integer minPurchase,
        Boolean duplicate,
        Category category,
        int totalCoupon,
        Date eventStartTime

) {
    public CouponEvent toCouponEventEntity(CouponEventRequestDto req, Coupon coupon){
        return CouponEvent.builder()
                .couponEventId(null)
                .coupon(coupon)
                .restCoupon(req.totalCoupon)
                .totalCoupon(req.totalCoupon)
                .eventStartTime(req.eventStartTime)
                .build();
    }

    public Coupon toCouponEntity(CouponEventRequestDto req){
        return Coupon.builder()
                .couponId(UUID.randomUUID())
                .couponName(req.couponName)
                .discountValue(req.discountValue)
                .validEnd(req.validEnd)
                .minPurchase(req.minPurchase)
                .duplicate(req.duplicate)
                .category(req.category)
                .discountType(DiscountType.valueOf(req.discountType))
                .build();
    }
}
