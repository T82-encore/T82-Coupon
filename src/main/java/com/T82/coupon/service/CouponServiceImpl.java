package com.T82.coupon.service;

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
import com.T82.coupon.global.domain.exception.*;
import com.T82.coupon.global.domain.repository.CouponBoxRepository;
import com.T82.coupon.global.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
//verifyCouponService
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;
    private final CouponBoxRepository couponBoxRepository;

    /**
     * 쿠폰 사용시 결제서비스 -> 쿠폰서비스
     */
    @KafkaListener(topics = "couponUsed")
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
    public void createCoupon(CouponRequestDto req) {
        couponRepository.save(req.toEntity(req));
    }

    /**
     * 카테고리별로 발급받을 수 있는 쿠폰 반환
     */
    @Override
    public Page<CouponResponseDto> getCouponsByCategory(String category, Pageable pageRequest) {
        Page<Coupon> allByCategory = couponRepository.findAllByCategory(Category.from(category), pageRequest);
        return allByCategory.map(CouponResponseDto::from);
    }

    /**
     * 쿠폰을 유저에게 지급
     */
    @Override
    public void giveCouponToUser(String couponId, UserDto userDto) {
        Coupon coupon = couponRepository.findById(UUID.fromString(couponId)).orElseThrow(CouponNotFoundException::new);
        couponBoxRepository.save(CouponBox.toEntity(coupon, userDto.getId()));
    }

    /**
     * 사용자가 가진 사용가능한 쿠폰 반환
     */
    @Override
    public Page<CouponResponseDto> getValidCoupons(Pageable pageable,UserDto userDto) {
        UserDto principal = (UserDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<Coupon> allByIds = couponBoxRepository.findAllByUserId(principal.getId(), pageable);
        return allByIds.map(CouponResponseDto::from);
    }

    /**
     * 사용가능한 쿠폰인지 검증
     */

    @Override
    @Transactional
    public CouponVerifyResponseDto verifyCoupons(CouponVerifyRequestDto req) {
        LocalDate today = LocalDate.now();

        req.items().forEach(couponUsage -> {
            couponUsage.couponIds().forEach(couponId -> {
                CouponBox couponBox = couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(couponId), req.userId())
                        .orElseThrow(CouponNotFoundException::new);

                log.error("Status {}",couponBox.getStatus());
                if (couponBox.getStatus() != Status.UNUSED) throw new ExpiredCouponException(); // 사용여부 검사

                Coupon coupon = couponBox.getId().getCoupon();
                log.error("validEnd {}",coupon.getValidEnd());
                if (coupon.getValidEnd().before(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()))) {
                    throw new ExpiredCouponException(); // 유효기간 검사
                }

                log.error("결제금액 {} == 쿠폰최소금액 {} ",couponUsage.beforeAmount(), coupon.getMinPurchase());
                if (!coupon.validateMinPurchase(couponUsage.beforeAmount())) {
                    throw new MinPurchaseException(); // 최소사용금액 검사
                }
            });
        });

        return CouponVerifyResponseDto.from("OK");
    }


    public CouponBox getCouponBox(String userId, String couponId) {
        return couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(couponId), userId)
                .orElseThrow(CouponNotFoundException::new);
    }


}
