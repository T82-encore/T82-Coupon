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
        Map<String,String> validateDuplicate = new HashMap<>();
        req.coupons().forEach(couponReq -> {
            Coupon coupon = getCouponBox(req.userId(), couponReq.couponId()).getId().getCoupon();
            CouponBox couponBox = couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(req.coupons().get(0).couponId()),req.userId()).orElseThrow(CouponNotFoundException::new);
            validateCoupon(validateDuplicate, couponReq, coupon, couponBox);
        });
        return CouponVerifyResponseDto.from("OK");
    }

    public void validateCoupon(Map<String, String> validateDuplicate, CouponVerifyRequestDto.CouponUsage couponReq, Coupon coupon, CouponBox couponBox) {
        if (couponBox.getStatus().validateIsExpired()) throw new ExpiredCouponException(); // 상태 검증
        if (coupon.validateMinPurchase(couponReq.beforeAmount())) throw new MinPurchaseException(); // 최소금액 검증
        validateNonDuplicateCoupon(validateDuplicate, couponReq.seatId(), couponReq.couponId()); // 중복쿠폰 검증
    }

    public CouponBox getCouponBox(String userId, String couponId) {
        return couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(couponId), userId)
                .orElseThrow(CouponNotFoundException::new);
    }

    public void validateNonDuplicateCoupon(Map<String,String> validMap, String seatId, String couponId) {
        if(validMap.containsKey(seatId)){
            Coupon coupon = couponRepository.findById(UUID.fromString(couponId)).orElseThrow(CouponNotFoundException::new);
            if (!coupon.getDuplicate()) {
                Coupon couponSaved = couponRepository.findById(UUID.fromString(validMap.get(seatId))).orElseThrow(CouponNotFoundException::new);
                if(!couponSaved.getDuplicate()) throw new DuplicateCouponException();
            }
        }else{
            validMap.put(seatId,couponId);
        }
    }
}
