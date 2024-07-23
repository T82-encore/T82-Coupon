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
public class CouponServiceImpl implements CouponService{
    private final CouponRepository couponRepository;
    private final CouponBoxRepository couponBoxRepository;

    /**
     * 쿠폰 사용시 결제서비스 -> 쿠폰서비스
     */
    @KafkaListener(topics = "coupon_used")
    @Transactional
    public void useCoupons(UseCouponRequestDto req) {
        log.error("Received request: couponIds={}, userId={}", req.couponIds().toString(), req.userId());

        req.couponIds().stream().distinct().forEach(couponId -> {
            try {
                Optional<CouponBox> optionalCouponBox = couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(couponId), req.userId());
                if (optionalCouponBox.isPresent()) {
                    CouponBox couponBox = optionalCouponBox.get();
                    couponBox.setStatus(Status.USED);
                } else {
                    log.error("CouponBox not found for couponId={} and userId={}", couponId, req.userId());
                }
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format for couponId={}", couponId, e);
            } catch (Exception e) {
                log.error("Error processing couponId={} for userId={}", couponId, req.userId(), e);
            }
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
    public void giveCouponToUser(String couponId, String userId) {
        Coupon coupon = couponRepository.findById(UUID.fromString(couponId)).orElseThrow(CouponNotFoundException::new);
        couponBoxRepository.save(CouponBox.toEntity(coupon,userId));
    }

    /**
     * 사용자가 가진 사용가능한 쿠폰 반환
     */
    @Override
    public Page<CouponResponseDto> getValidCoupons(Pageable pageable) {
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
            Coupon coupon = getCouponBox(req.userId(), couponReq.couponId()).get().getId().getCoupon();
            Optional<CouponBox> couponBox = couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(req.coupons().get(0).couponId()),req.userId());
            validateIsExpired(couponBox.get().getStatus()); // 상태 검증
            validateMinPurchase(couponReq.beforeAmount(), coupon); // 최소금액 검증
            validateNonDuplicateCoupon(validateDuplicate,couponReq.seatId(), couponReq.couponId()); // 중복쿠폰 검증
        });
        return CouponVerifyResponseDto.from("OK");
    }



    public Optional<CouponBox> getCouponBox(String userId, String couponId) {
        Optional<CouponBox> couponBoxOpt = couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(couponId), userId);
        if (couponBoxOpt.isEmpty()) throw new CouponNotFoundException();
        return couponBoxOpt;
    }

    public void validateNonDuplicateCoupon(Map<String,String> validMap, String seatId, String couponId) {
        if(validMap.containsKey(seatId)){
            Coupon coupon = couponRepository.findById(UUID.fromString(couponId)).get();
            if (!coupon.getDuplicate()) {
                Coupon coupon1 = couponRepository.findById(UUID.fromString(validMap.get(seatId))).get();
                if(!coupon1.getDuplicate()) throw new DuplicateCouponException();
            }
        }else{
            validMap.put(seatId,couponId);
        }
    }

    public void validateMinPurchase(int amount, Coupon coupon) {
        if (coupon.getMinPurchase() > amount) {
            throw new MinPurchaseException();
        }
    }

    public void validateIsExpired(Status status) {
        if (status!=Status.UNUSED) throw new ExpiredCouponException();
    }
}
