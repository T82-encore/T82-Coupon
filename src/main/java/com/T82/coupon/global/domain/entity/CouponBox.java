package com.T82.coupon.global.domain.entity;

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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOX_ID")
    private String boxId;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "IS_USED")
    private Boolean isUsed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUPON_ID", nullable = false)
    private Coupon coupon;

    public static CouponBox toEntity(Coupon coupon,String userId){
        return CouponBox.builder()
                .boxId(null)
                .userId(userId)
                .isUsed(false)
                .coupon(coupon)
                .build();
    }
}
