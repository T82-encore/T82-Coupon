package com.T82.coupon.global.domain.entity;

import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.enums.DiscountType;
import com.T82.coupon.global.domain.exception.MinPurchaseException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "COUPONS")
public class Coupon {
    @Id
    @Column(name = "COUPON_ID")
    private UUID couponId;
    @Column(name = "COUPON_NAME")
    private String couponName;
    @Column(name = "DISCOUNT_VALUE")
    private Integer discountValue;
    @Column(name = "VALID_END")
    private Date validEnd;
    @Column(name = "MIN_PURCHASE")
    private Integer minPurchase;
    @Column(name = "DUPLICATE")
    private Boolean duplicate;
    @Enumerated(EnumType.STRING)
    private Category category;
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @OneToMany(mappedBy = "id.coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CouponBox> couponBoxes = new ArrayList<>();

    public boolean validateMinPurchase(int amount) {
        return minPurchase > amount;
    }
}
