package com.T82.coupon.global.domain.dto;

import lombok.Builder;

@Builder
public record IssueCouponDto(
    String userId,
    String couponId
) {
    public static IssueCouponDto toDto(String couponId, String userId) {
        return IssueCouponDto.builder()
                .couponId(couponId)
                .userId(userId)
                .build();
    }
}