package com.T82.coupon.service;

import com.T82.common_exception.annotation.CustomException;
import com.T82.common_exception.exception.ErrorCode;
import com.T82.common_exception.exception.coupon.CouponAlreadyIssuedException;
import com.T82.common_exception.exception.coupon.CouponExpiredException;
import com.T82.common_exception.exception.coupon.CouponNotFoundException;
import com.T82.common_exception.exception.coupon.CouponValidateFailedException;
import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.dto.request.CouponVerifyRequestDto;
import com.T82.coupon.dto.request.UseCouponRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
import com.T82.coupon.dto.response.CouponVerifyResponseDto;
import com.T82.coupon.global.domain.dto.UserDto;
import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.entity.CouponBox;
import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.enums.Status;
import com.T82.coupon.global.domain.repository.CouponBoxRepository;
import com.T82.coupon.global.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;
    private final CouponBoxRepository couponBoxRepository;
    /**
     * 쿠폰 사용시 결제서비스 -> 쿠폰서비스
     */
    @KafkaListener(topics = "couponUsed",groupId = "coupon-group")
    @Transactional
    public void useCoupons(UseCouponRequestDto req) {
        req.couponIds().stream().distinct().forEach(couponId -> {
            CouponBox couponBox = getCouponBox(req.userId(), couponId);
            couponBox.setStatus(Status.USED);
        });
    }

    /**
     * 쿠폰 생성 (관리자)
     */
    @Override
    @CustomException(ErrorCode.FAILED_GENERATE_COUPON)
    public void createCoupon(CouponRequestDto req) {
        couponRepository.save(req.toEntity(req));
    }

    /**
     * 카테고리별로 발급받을 수 있는 쿠폰 반환
     */
    @Override
    @CustomException(ErrorCode.COUPON_NOT_FOUND)
    public List<CouponResponseDto> getCouponsByCategory(String category) {
        List<Coupon> allByCategory = couponRepository.findAllByCategory(Category.from(category));
        return allByCategory.stream().map(CouponResponseDto::from).toList();
    }

    /**
     * 쿠폰을 유저에게 지급
     */
    @Override
    @CustomException(ErrorCode.FAILED_ISSUE_COUPON)
    public void giveCouponToUser(String couponId, String userId) {
        Coupon coupon = couponRepository.findById(UUID.fromString(couponId)).orElseThrow(CouponNotFoundException::new);
        couponBoxRepository.save(CouponBox.toEntity(coupon, userId));
    }

    /**
     * 사용자가 가진 사용가능한 쿠폰 반환
     */
    @Override
    @CustomException(ErrorCode.COUPON_NOT_FOUND)
    public List<CouponResponseDto> getValidCoupons(UserDto userDto) {
        List<Coupon> allByIds = couponBoxRepository.findAllByUserId(userDto.getId());
        return allByIds.stream().map(CouponResponseDto::from).toList();
    }

    /**
     * 사용가능한 쿠폰인지 검증
     */
    @Override
    @Transactional
    @CustomException(ErrorCode.COUPON_VALIDATE_FAILED)
    public CouponVerifyResponseDto verifyCoupons(CouponVerifyRequestDto req) {
        LocalDate today = LocalDate.now();
        req.items().forEach(couponUsage -> {
            couponUsage.couponIds().forEach(couponId -> {
                Coupon coupon = validateIsUsed(req, couponId);
                validateExpired(today, coupon);
                validateMinPurchase(couponUsage, coupon);
            });
        });
        return CouponVerifyResponseDto.from("OK");
    }




    private static void validateMinPurchase(CouponVerifyRequestDto.CouponUsage couponUsage, Coupon coupon) {
        if (!coupon.validateMinPurchase(couponUsage.beforeAmount())) {
            throw new CouponValidateFailedException(); // 최소사용금액 검사
        }
    }

    private static void validateExpired(LocalDate today, Coupon coupon) {
        if (coupon.getValidEnd().before(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()))) {
            throw new CouponExpiredException(); // 유효기간 검사
        }
    }

    private Coupon validateIsUsed(CouponVerifyRequestDto req, String couponId) {
        CouponBox couponBox = couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(couponId), req.userId())
                .orElseThrow(IllegalArgumentException::new);
        if (couponBox.getStatus() != Status.UNUSED) throw new CouponAlreadyIssuedException(); // 사용여부 검사
        return couponBox.getId().getCoupon();
    }

    public CouponBox getCouponBox(String userId, String couponId) {
        return couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(couponId), userId)
                .orElseThrow(CouponNotFoundException::new);
    }
}
