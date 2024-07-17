package com.T82.coupon.service;

import com.T82.coupon.dto.request.CreateCouponRequestDto;
import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService{
    private final CouponRepository couponRepository;
    @Override
    public void createCoupon(CreateCouponRequestDto req) {
        couponRepository.save(req.toEntity(req));
    }
}
