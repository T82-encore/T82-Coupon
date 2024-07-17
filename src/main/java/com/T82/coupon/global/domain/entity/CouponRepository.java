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
@Table(name = "COUPON_REPOSITORIES")
public class CouponRepository {
    @Id
    @Column(name = "COUPON_ID")
    private Long couponId;
    @Column(name = "USER_ID")
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Coupon coupon;
}
