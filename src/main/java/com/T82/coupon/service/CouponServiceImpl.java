package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService{
    private final CouponRepository couponRepository;
    @Override
    public void createCoupon(CouponRequestDto req) {
        couponRepository.save(req.toEntity(req));
    }

    @Override
    public Page<CouponResponseDto> getCouponsByCategory(Category category, Pageable pageRequest) {
        Page<Coupon> allByCategory = couponRepository.findAllByCategory(category, pageRequest);
        return allByCategory.map(CouponResponseDto::from);
    }
}
