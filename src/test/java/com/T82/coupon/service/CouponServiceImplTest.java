package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.enums.DiscountType;
import com.T82.coupon.global.domain.exception.CategoryNotFoundException;
import com.T82.coupon.global.domain.repository.CouponRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

import static com.T82.coupon.global.domain.enums.Category.SPORTS;
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
            CouponRequestDto coupon = new CouponRequestDto("테스트쿠폰", DiscountType.FIXED, 1000, Date.from(Instant.parse("2024-12-31T23:59:59.59Z")) , 10000, true, SPORTS);
            int lengthBefore = couponRepository.findAll().size();
//    when
            couponService.createCoupon(coupon);
//    then
            assertEquals(couponRepository.findAll().size(),lengthBefore + 1);
        }
    }

    @Nested
    @Transactional
    class 카테고리별_쿠폰_가져오기 {
        @Test
        void 페이징_테스트_성공() {
            // given
            CouponRequestDto coupon1 = new CouponRequestDto("테스트쿠폰1", DiscountType.FIXED, 1000, Date.from(Instant.parse("2024-12-31T23:58:59.00Z")), 10000, true, SPORTS);
            CouponRequestDto coupon2 = new CouponRequestDto("테스트쿠폰2", DiscountType.FIXED, 2000, Date.from(Instant.parse("2024-12-31T23:59:59.00Z")), 20000, true, SPORTS);
            CouponRequestDto coupon3 = new CouponRequestDto("테스트쿠폰3", DiscountType.PERCENTAGE, 15, Date.from(Instant.parse("2024-12-31T23:59:59.00Z")), 5000, true, SPORTS);

            couponRepository.save(coupon1.toEntity(coupon1));
            couponRepository.save(coupon2.toEntity(coupon2));
            couponRepository.save(coupon3.toEntity(coupon3));

            // when
            Pageable pageable = PageRequest.of(0, 2);

            Page<CouponResponseDto> result = couponService.getCouponsByCategory(String.valueOf(SPORTS), pageable);
            // then
            assertEquals(2, result.getNumberOfElements());
            assertEquals(3, result.getTotalElements());
            assertEquals(2, result.getTotalPages());
            assertTrue(result.getContent().stream().allMatch(coupon -> coupon.category() == SPORTS));
            // 쿠폰이 날짜 순으로 정렬되었는지 확인
            assertTrue(result.getContent().get(0).validEnd().before(result.getContent().get(1).validEnd()));

            // 추가로 다른 페이지를 요청하여 내용 확인
            Pageable pageableSecondPage = PageRequest.of(1, 2);
            Page<CouponResponseDto> resultSecondPage = couponService.getCouponsByCategory(String.valueOf(SPORTS), pageableSecondPage);

            assertEquals(1, resultSecondPage.getNumberOfElements());
        }

        @Test
        void 없는_Categroy값이_들어왔을때_실패_테스트() {
            // given
            CouponRequestDto coupon1 = new CouponRequestDto("테스트쿠폰3", DiscountType.PERCENTAGE, 15, Date.from(Instant.parse("2024-12-31T23:59:59.00Z")), 5000, true, SPORTS);
            couponRepository.save(coupon1.toEntity(coupon1));

            // when
            Pageable pageable = PageRequest.of(0, 5); // 페이지 크기를 크게 지정하여 모든 결과를 가져오도록 함
            CategoryNotFoundException categoryNotFoundException = assertThrows(CategoryNotFoundException.class,()-> couponService.getCouponsByCategory(String.valueOf(Category.from("hi")), pageable)); // 존재하지 않는 category값 전달
            // then
            assertEquals("Not Fount Category",categoryNotFoundException.getMessage());
        }
    }
}