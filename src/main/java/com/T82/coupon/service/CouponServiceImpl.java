package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
import com.T82.coupon.global.domain.dto.UserDto;
import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.entity.CouponBox;
import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.exception.CouponNotFoundException;
import com.T82.coupon.global.domain.repository.CouponBoxRepository;
import com.T82.coupon.global.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService{
    private final CouponRepository couponRepository;
    private final CouponBoxRepository couponBoxRepository;
    @Override
    public void createCoupon(CouponRequestDto req) {
        couponRepository.save(req.toEntity(req));
    }

    @Override
    public Page<CouponResponseDto> getCouponsByCategory(String category, Pageable pageRequest) {
        Page<Coupon> allByCategory = couponRepository.findAllByCategory(Category.from(category), pageRequest);
        return allByCategory.map(CouponResponseDto::from);
    }

    @Override
    public void giveCouponToUser(String couponId, String userId) {
        Coupon coupon = couponRepository.findById(UUID.fromString(couponId)).orElseThrow(CouponNotFoundException::new);
        couponBoxRepository.save(CouponBox.toEntity(coupon,userId));
    }

    @Override
    public Page<CouponResponseDto> getValidCoupons(Pageable pageable) {
        UserDto principal = (UserDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<Coupon> allByIds = couponBoxRepository.findAllByUserId(principal.getId(), pageable);
        return allByIds.map(CouponResponseDto::from);
    }
}
