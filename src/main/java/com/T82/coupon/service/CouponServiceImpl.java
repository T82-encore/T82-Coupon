package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.dto.request.CouponVerifyRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
import com.T82.coupon.dto.response.CouponVerifyResponseDto;
import com.T82.coupon.global.domain.dto.UserDto;
import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.entity.CouponBox;
import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.exception.*;
import com.T82.coupon.global.domain.repository.CouponBoxRepository;
import com.T82.coupon.global.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static com.T82.coupon.utils.CouponUtil.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService{
    private final CouponRepository couponRepository;
    private final CouponBoxRepository couponBoxRepository;

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
        // 조건1: 중복 불가능한 쿠폰이 하나만 있어야 한다.
        final boolean[] hasNonDuplicateCoupon = {false};

        req.coupons().forEach(couponReq -> {
            Coupon coupon = getCouponBox(req.userId(), couponReq.couponId()).get().getId().getCoupon();
            // 조건1: 중복 불가능한 쿠폰이 하나만 있어야 한다.
            hasNonDuplicateCoupon[0] = validateNonDuplicateCoupon(hasNonDuplicateCoupon[0], coupon);
            // 조건2: 최소가능 금액이 amount 이상이어야 한다.
            validateMinPurchase(req.amount(), coupon);
            // 조건3: 입력 받은 Category가 쿠폰의 Category와 같거나 ALL이여야 한다.
            validateCategory(Category.valueOf(req.category()), coupon);
        });

        // 검증 결과를 반환
        return CouponVerifyResponseDto.from("OK");
    }
    public Optional<CouponBox> getCouponBox(String userId, String couponId) {
        Optional<CouponBox> couponBoxOpt = couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(couponId), userId);
        if (couponBoxOpt.isEmpty()) throw new CouponNotFoundException();
        return couponBoxOpt;
    }

}
