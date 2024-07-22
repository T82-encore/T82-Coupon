package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.dto.request.CouponVerifyRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
import com.T82.coupon.dto.response.CouponVerifyResponseDto;
import com.T82.coupon.global.domain.dto.UserDto;
import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.entity.CouponBox;
import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.enums.DiscountType;
import com.T82.coupon.global.domain.exception.CategoryNotFoundException;
import com.T82.coupon.global.domain.exception.CouponNotFoundException;
import com.T82.coupon.global.domain.repository.CouponBoxRepository;
import com.T82.coupon.global.domain.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.T82.coupon.global.domain.enums.Category.SPORTS;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CouponServiceImplTest {
    @Autowired
    CouponServiceImpl couponService;

    @Autowired
    CouponRepository couponRepository;
    @Autowired
    CouponBoxRepository couponBoxRepository;
    String userId;
    @BeforeEach
    void setUp() {
        userId = "testUserId";
        UserDto principal = new UserDto(userId, "test@example.com");
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @Transactional
    class 쿠폰생성 {
        @Test
        void 성공() {
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
            assertEquals("존재하지 않는 카테고리 입니다.",categoryNotFoundException.getMessage());
        }
    }

    @Nested
    @Transactional
    class 유저의_쿠폰함에_쿠폰_지급_테스트 {
        @Test
        void 유저의_쿠폰함에_쿠폰_지급_성공() {
            // given
            String userId = "testUserId";
            CouponRequestDto couponRequestDto = new CouponRequestDto(
                    "테스트쿠폰",
                    DiscountType.PERCENTAGE,
                    15,
                    Date.from(Instant.parse("2024-12-31T23:59:59.00Z")),
                    5000,
                    true,
                    SPORTS
            );
            Coupon coupon = couponRepository.saveAndFlush(couponRequestDto.toEntity(couponRequestDto));
            UUID couponId = coupon.getCouponId();

            // when
            couponService.giveCouponToUser(couponId.toString(), userId);

            // then
            CouponBox savedCouponBox = couponBoxRepository.findAll().get(0);
            assertEquals(couponId, savedCouponBox.getId().getCoupon().getCouponId());
            assertEquals(userId, savedCouponBox.getId().getUserId());
        }

        @Test
        void 유저의_쿠폰함에_쿠폰_지급_실패_쿠폰_없음() {
            // given
            String couponId = UUID.randomUUID().toString();
            String userId = "testUserId";

            // when & then
            assertThrows(CouponNotFoundException.class, () -> {
                couponService.giveCouponToUser(couponId, userId);
            });
        }
    }

    @Nested
    @Transactional
    class 유저의_유효한_쿠폰_반환_테스트 {
        @Test
        void 반환_성공() {
            // given
            String userId = "testUserId";
            String userId2 = "testUserId2";
            CouponRequestDto couponRequestDto = new CouponRequestDto(
                    "테스트쿠폰",
                    DiscountType.PERCENTAGE,
                    15,
                    Date.from(Instant.parse("2024-12-31T23:59:59.00Z")),
                    5000,
                    true,
                    SPORTS
            );
            Coupon coupon = couponRepository.saveAndFlush(couponRequestDto.toEntity(couponRequestDto));
            UUID couponId = coupon.getCouponId();
            CouponRequestDto couponRequestDto2 = new CouponRequestDto(
                    "테스트쿠폰",
                    DiscountType.PERCENTAGE,
                    15,
                    Date.from(Instant.parse("2024-12-31T23:59:59.00Z")),
                    5000,
                    true,
                    SPORTS
            );
            Coupon coupon2 = couponRepository.saveAndFlush(couponRequestDto2.toEntity(couponRequestDto2));
            UUID couponId2 = coupon2.getCouponId();
            couponService.giveCouponToUser(couponId.toString(), userId);
            couponService.giveCouponToUser(couponId2.toString(), userId2);

            Pageable pageable = PageRequest.of(0, 2);
            // when
            Page<CouponResponseDto> validCoupons = couponService.getValidCoupons(pageable);
            // then
            assertEquals(1, validCoupons.getNumberOfElements());
            assertEquals(couponId,validCoupons.getContent().get(0).couponId());
        }
    }

    @Nested
    @Transactional
    class 쿠폰_검증{
        LocalDate localDate = LocalDate.of(3000, 12, 31);
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        @Test
        void 성공() throws ParseException {
//          given
            // 쿠폰 3개 생성
            Coupon 쿠폰1 = couponRepository.save(Coupon.builder().couponId(UUID.randomUUID()).couponName("쿠폰1").discountType(DiscountType.FIXED).discountValue(1000).validEnd(date).minPurchase(10000).duplicate(false).category(SPORTS).build());
            Coupon 쿠폰2 = couponRepository.save(Coupon.builder().couponId(UUID.randomUUID()).couponName("쿠폰2").discountType(DiscountType.FIXED).discountValue(1000).validEnd(date).minPurchase(10000).duplicate(true).category(SPORTS).build());
            Coupon 쿠폰3 = couponRepository.save(Coupon.builder().couponId(UUID.randomUUID()).couponName("쿠폰3").discountType(DiscountType.FIXED).discountValue(1000).validEnd(date).minPurchase(10000).duplicate(true).category(SPORTS).build());
            // 쿠폰함에 3개 넣고
            couponBoxRepository.save(CouponBox.toEntity(쿠폰1,userId));
            couponBoxRepository.save(CouponBox.toEntity(쿠폰2,userId));
            couponBoxRepository.save(CouponBox.toEntity(쿠폰3,userId));
            // CouponVerifyRequestDto 생성
            List<CouponVerifyRequestDto.CouponUsage> couponUsages = List.of(
                    new CouponVerifyRequestDto.CouponUsage(쿠폰1.getCouponId().toString()),
                    new CouponVerifyRequestDto.CouponUsage(쿠폰2.getCouponId().toString()),
                    new CouponVerifyRequestDto.CouponUsage(쿠폰3.getCouponId().toString())
            );
            CouponVerifyResponseDto couponVerifyResponseDto = couponService.verifyCoupons(new CouponVerifyRequestDto(userId, 100000, SPORTS.toString(), couponUsages));
//            when
            assertEquals("OK",couponVerifyResponseDto.status());
        }
    }
}