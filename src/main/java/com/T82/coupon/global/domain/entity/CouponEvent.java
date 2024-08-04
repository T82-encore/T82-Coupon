package com.T82.coupon.global.domain.entity;

import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.enums.DiscountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "COUPON_EVENTS")
public class CouponEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUPON_EVENT_ID")
    private Long couponEventId;
    @Column(name = "TOTAL_COUPON")
    private int totalCoupon;
    @Column(name = "REST_COUPON")
    private int restCoupon;
    @Column(name = "EVENT_START_TIME")
    private Date eventStartTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUPON_ID")
    private Coupon coupon;
}
