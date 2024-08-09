package com.T82.coupon.utils.gRPC;

import com.T82.common_exception.annotation.CustomException;
import com.T82.common_exception.exception.ErrorCode;
import com.T82.common_exception.exception.coupon.CouponAlreadyIssuedException;
import com.T82.common_exception.exception.coupon.CouponExpiredException;
import com.T82.common_exception.exception.coupon.CouponNotFoundException;
import com.T82.common_exception.exception.coupon.MinPurchaseNotAcceptedException;
import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.entity.CouponBox;
import com.T82.coupon.global.domain.enums.Status;
import com.T82.coupon.global.domain.repository.CouponBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrpcUtil {
    private final CouponBoxRepository couponBoxRepository;

    @CustomException(ErrorCode.COUPON_VALIDATE_FAILED)
    public Coupon validateIsUsed(String userId, String couponId) {
        CouponBox couponBox = couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(couponId), userId)
                .orElseThrow(CouponNotFoundException::new);
        if (couponBox.getStatus() != Status.UNUSED) throw new CouponAlreadyIssuedException(); // 사용여부 검사
        return couponBox.getId().getCoupon();
    }

    @CustomException(ErrorCode.COUPON_EXPIRED)
    public void validateExpired(Coupon coupon) {
        LocalDate now = LocalDate.now();
        if (coupon.getValidEnd().before(Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant()))) {
            throw new CouponExpiredException(); // 유효기간 검사
        }
    }

    @CustomException(ErrorCode.MIN_PURCHASE_NOT_ACCEPTED)
    public void validateMinPurchase(int beforeAmount, Coupon coupon) {
        if (!coupon.validateMinPurchase(beforeAmount)) {
            throw new MinPurchaseNotAcceptedException(); // 최소사용금액 검사
        }
    }
}
