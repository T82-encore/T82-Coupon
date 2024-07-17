package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.enums.DiscountType;
import com.T82.coupon.global.domain.repository.CouponRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CouponServiceImplTest {
    @Autowired
    CouponServiceImpl couponService;

    @Autowired
    CouponRepository couponRepository;

    @Nested
    @Transactional
    class 쿠폰생성 {
        @Test
        void 쿠폰생성_성공() {
//    given
            CouponRequestDto coupon = new CouponRequestDto("테스트쿠폰", DiscountType.FIXED, 1000, Date.from(Instant.parse("2024-12-31T23:59:59.00Z.")) , 10000, true, Category.SPORTS);
            int lengthBefore = couponRepository.findAll().size();
//    when
            couponService.createCoupon(coupon);
//    then
            assertEquals(couponRepository.findAll().size(),lengthBefore + 1);
        }

    }
}