package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponEventRequestDto;
import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.repository.CouponEventRepository;
import com.T82.coupon.global.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponEventServiceImpl implements CouponEventService{
    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;

    /**
     * 쿠폰 이벤트 생성 (쿠폰+이벤트 생성)
     */
    @Override
    @Transactional
    public void createCouponEvent(CouponEventRequestDto req) {
        Coupon savedCoupon = couponRepository.save(req.toCouponEntity(req));
        couponEventRepository.save(req.toCouponEventEntity(req,savedCoupon));
    }


}
