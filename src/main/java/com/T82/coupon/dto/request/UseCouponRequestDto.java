package com.T82.coupon.dto.request;

import java.util.List;

public record UseCouponRequestDto(
        String userId,
        List<String> couponIds
) {
}