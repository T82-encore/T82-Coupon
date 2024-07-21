package com.T82.coupon.global.domain.entity;

import com.T82.coupon.global.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "COUPON_BOXES")
public class CouponBox {

    @EmbeddedId
    private CouponBoxId id;

    @Column(name = "STATUS")
    private Status status;

    public static CouponBox toEntity(Coupon coupon, String userId) {
        CouponBoxId couponBoxId = new CouponBoxId(userId, coupon);
        return CouponBox.builder()
                .id(couponBoxId)
                .status(Status.UNUSED)
                .build();
    }
}
