package com.T82.coupon.global.domain.entity;

import com.T82.coupon.global.domain.enums.DiscountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "COUPONS")
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUPON_ID")
    private Long couponId;
    @Column(name = "COUPON_NAME")
    private String couponName;
    @Column(name = "DISCOUNT_VALUE")
    private Integer discountValue;
    @Column(name = "VALID_START")
    private Integer validStart;
    @Column(name = "VALID_END")
    private Integer validEnd;
    @Column(name = "MIN_PURCHASE")
    private Integer minPurchase;
    @Column(name = "DUPLICATE")
    private Boolean duplicate;
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Coupon> coupons = new ArrayList<>();

}
