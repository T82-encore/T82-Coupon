package com.T82.coupon.global.domain.entity;

import com.T82.coupon.global.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "COUPON_BOXES")
public class CouponBox {

    @EmbeddedId
    private CouponBoxId id;

    @Column(name = "STATUS") @Setter
    private Status status;

    public static CouponBox toEntity(Coupon coupon, String userId) {
        CouponBoxId couponBoxId = new CouponBoxId(userId, coupon);
        return CouponBox.builder()
                .id(couponBoxId)
                .status(Status.UNUSED)
                .build();
    }
}
