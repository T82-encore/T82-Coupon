package com.T82.coupon.service;

import com.T82.common_exception.exception.CustomException;
import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.dto.request.CouponVerifyRequestDto;
import com.T82.coupon.dto.request.UseCouponRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
import com.T82.coupon.dto.response.CouponVerifyResponseDto;
import com.T82.coupon.global.domain.dto.UserDto;
import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.entity.CouponBox;
import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.enums.DiscountType;
import com.T82.coupon.global.domain.enums.Status;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
            // given
            CouponRequestDto coupon = new CouponRequestDto("테스트쿠폰", DiscountType.FIXED, 1000, Date.from(Instant.parse("2024-12-31T23:59:59.59Z")) , 10000, true, SPORTS);
            int lengthBefore = couponRepository.findAll().size();
            // when
            couponService.createCoupon(coupon);
            // then
            assertEquals(couponRepository.findAll().size(), lengthBefore + 1);
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
            // then
            assertThrows(IllegalArgumentException.class, () -> couponService.getCouponsByCategory(String.valueOf(Category.from("hi")), pageable)); // 존재하지 않는 category값 전달
        }
    }

    @Nested
    @Transactional
    class 유저의_쿠폰함에_쿠폰_지급_테스트 {
        @Test
        void 유저의_쿠폰함에_쿠폰_지급_성공() {
            // given
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
            UserDto principal = (UserDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // when
            couponService.giveCouponToUser(couponId.toString(), principal.getId());

            // then
            CouponBox savedCouponBox = couponBoxRepository.findAll().get(0);
            assertEquals(couponId, savedCouponBox.getId().getCoupon().getCouponId());
            assertEquals(principal.getId(), savedCouponBox.getId().getUserId());
        }

        @Test
        void 유저의_쿠폰함에_쿠폰_지급_실패_쿠폰_없음() {
            // given
            String couponId = UUID.randomUUID().toString();
            UserDto principal = (UserDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // when & then
            assertThrows(CustomException.class, () -> {
                couponService.giveCouponToUser(couponId, principal.getId());
            });
        }
    }

    @Nested
    @Transactional
    class 유저의_유효한_쿠폰_반환_테스트 {
        @Test
        void 반환_성공() {
            // given
            UserDto principal = (UserDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
                    "테스트쿠폰2",
                    DiscountType.PERCENTAGE,
                    15,
                    Date.from(Instant.parse("2024-12-31T23:59:59.00Z")),
                    5000,
                    true,
                    SPORTS
            );
            Coupon coupon2 = couponRepository.saveAndFlush(couponRequestDto2.toEntity(couponRequestDto2));
            UUID couponId2 = coupon2.getCouponId();
            couponService.giveCouponToUser(couponId.toString(), principal.getId());
            couponService.giveCouponToUser(couponId2.toString(), principal.getId());

            Pageable pageable = PageRequest.of(0, 2);
            // when
            Page<CouponResponseDto> validCoupons = couponService.getValidCoupons(pageable, principal);
            // then
            assertEquals(2, validCoupons.getNumberOfElements());
            assertTrue(validCoupons.getContent().stream().anyMatch(couponDto -> couponDto.couponId().equals(couponId)));
            assertTrue(validCoupons.getContent().stream().anyMatch(couponDto -> couponDto.couponId().equals(couponId2)));
        }
    }
    @Nested
    @Transactional
    class 쿠폰_검증 {

        LocalDate futureDate = LocalDate.of(3000, 12, 31);
        Date futureDateAsDate = Date.from(futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        LocalDate currentDate = LocalDate.now();
        Date currentDateAsDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String userId = "testUser";


        @Test
        void 성공() {
            // given
            Coupon coupon1 = couponRepository.save(Coupon.builder()
                    .couponId(UUID.randomUUID())
                    .couponName("쿠폰1")
                    .discountValue(1000)
                    .validEnd(futureDateAsDate)
                    .minPurchase(10000)
                    .duplicate(false)
                    .category(Category.SPORTS)
                    .discountType(DiscountType.FIXED)
                    .build());
            Coupon coupon2 = couponRepository.save(Coupon.builder()
                    .couponId(UUID.randomUUID())
                    .couponName("쿠폰2")
                    .discountValue(1000)
                    .validEnd(futureDateAsDate)
                    .minPurchase(10000)
                    .duplicate(true)
                    .category(Category.SPORTS)
                    .discountType(DiscountType.FIXED)
                    .build());
            Coupon coupon3 = couponRepository.save(Coupon.builder()
                    .couponId(UUID.randomUUID())
                    .couponName("쿠폰3")
                    .discountValue(1000)
                    .validEnd(futureDateAsDate)
                    .minPurchase(10000)
                    .duplicate(true)
                    .category(Category.SPORTS)
                    .discountType(DiscountType.FIXED)
                    .build());

            couponBoxRepository.save(CouponBox.toEntity(coupon1, userId));
            couponBoxRepository.save(CouponBox.toEntity(coupon2, userId));
            couponBoxRepository.save(CouponBox.toEntity(coupon3, userId));

            List<CouponVerifyRequestDto.CouponUsage> couponUsages = List.of(
                    new CouponVerifyRequestDto.CouponUsage(List.of(coupon1.getCouponId().toString()), 10000),
                    new CouponVerifyRequestDto.CouponUsage(List.of(coupon2.getCouponId().toString()), 10000),
                    new CouponVerifyRequestDto.CouponUsage(List.of(coupon3.getCouponId().toString()), 10000)
            );

            // when
            CouponVerifyResponseDto response = couponService.verifyCoupons(new CouponVerifyRequestDto(userId, couponUsages));

            // then
            assertEquals("OK", response.result());
        }


        @Test
        void 최소금액_실패테스트() {
            // given
            Coupon coupon = couponRepository.save(Coupon.builder()
                    .couponId(UUID.randomUUID())
                    .couponName("쿠폰1")
                    .discountValue(1000)
                    .validEnd(futureDateAsDate)
                    .minPurchase(10000)
                    .duplicate(true)
                    .category(Category.SPORTS)
                    .discountType(DiscountType.FIXED)
                    .build());

            couponBoxRepository.save(CouponBox.toEntity(coupon, userId));

            List<CouponVerifyRequestDto.CouponUsage> couponUsages = List.of(
                    new CouponVerifyRequestDto.CouponUsage(List.of(coupon.getCouponId().toString()), 1000)
            );

            // when & then
            assertThrows(CustomException.class, () -> couponService.verifyCoupons(new CouponVerifyRequestDto(userId, couponUsages)));
        }
        @Test
        void 사용완료_쿠폰_실패_테스트() {
            // given
            Coupon coupon = couponRepository.save(Coupon.builder()
                    .couponId(UUID.randomUUID())
                    .couponName("사용 완료 쿠폰")
                    .discountValue(1000)
                    .validEnd(futureDateAsDate)
                    .minPurchase(10000)
                    .duplicate(true)
                    .category(Category.SPORTS)
                    .discountType(DiscountType.FIXED)
                    .build());

            // 쿠폰을 사용 완료 상태로 설정
            CouponBox couponBox = CouponBox.toEntity(coupon, userId);
            couponBox.setStatus(Status.USED); // 상태를 USED로 설정
            couponBoxRepository.save(couponBox);

            List<CouponVerifyRequestDto.CouponUsage> couponUsages = List.of(
                    new CouponVerifyRequestDto.CouponUsage(List.of(coupon.getCouponId().toString()), 10000)
            );

            // when & then
            assertThrows(CustomException.class, () -> couponService.verifyCoupons(new CouponVerifyRequestDto(userId, couponUsages)));
        }
        @Test
        void 쿠폰_만료_실패테스트() {
            // given
            Coupon expiredCoupon = couponRepository.save(Coupon.builder()
                    .couponId(UUID.randomUUID())
                    .couponName("Expired Coupon")
                    .discountValue(1000)
                    .validEnd(currentDateAsDate)
                    .minPurchase(1000)
                    .duplicate(true)
                    .category(Category.SPORTS)
                    .discountType(DiscountType.FIXED)
                    .build());
            Coupon validCoupon = couponRepository.save(Coupon.builder()
                    .couponId(UUID.randomUUID())
                    .couponName("Valid Coupon")
                    .discountValue(1000)
                    .validEnd(futureDateAsDate)
                    .minPurchase(1000)
                    .duplicate(false)
                    .category(Category.SPORTS)
                    .discountType(DiscountType.FIXED)
                    .build());

            couponBoxRepository.save(CouponBox.toEntity(expiredCoupon, userId));
            couponBoxRepository.save(CouponBox.toEntity(validCoupon, userId));

            CouponBox expiredCouponBox = couponBoxRepository.findByCouponIdAndUserId(expiredCoupon.getCouponId(), userId).orElseThrow();
            expiredCouponBox.setStatus(Status.EXPIRED); // 상태를 만료로 설정
            couponBoxRepository.save(expiredCouponBox);

            List<CouponVerifyRequestDto.CouponUsage> couponUsages = List.of(
                    new CouponVerifyRequestDto.CouponUsage(List.of(expiredCoupon.getCouponId().toString()), 10000)
            );

            // when & then
            assertThrows(CustomException.class, () -> couponService.verifyCoupons(new CouponVerifyRequestDto(userId, couponUsages)));
        }
    }




        @Nested
        @Transactional
        class 결제시_쿠폰상태_변경_테스트 {
            @Test
            void 성공() {
                // Given
                Coupon coupon = Coupon.builder()
                        .couponId(UUID.randomUUID())
                        .couponName("Test Coupon")
                        .discountValue(10)
                        .validEnd(new Date())
                        .minPurchase(100)
                        .duplicate(false)
                        .category(SPORTS)
                        .discountType(DiscountType.PERCENTAGE)
                        .build();
                couponRepository.save(coupon);

                CouponBox couponBox = CouponBox.toEntity(coupon, "testUserId");
                couponBoxRepository.save(couponBox);

                String userId = "testUserId";
                String couponId = coupon.getCouponId().toString();

                UseCouponRequestDto requestDto = new UseCouponRequestDto(userId, List.of(couponId));

                // Act
                couponService.useCoupons(requestDto);

                // Then
                Optional<CouponBox> updatedCouponBox = couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(couponId), userId);
                assertTrue(updatedCouponBox.isPresent());
                assertEquals(Status.USED, updatedCouponBox.get().getStatus()); // Validate that the coupon status is updated
            }

    }
}