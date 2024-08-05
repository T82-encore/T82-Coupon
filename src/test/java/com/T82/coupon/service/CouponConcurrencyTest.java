package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.global.domain.dto.UserDto;
import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.entity.CouponEvent;
import com.T82.coupon.global.domain.enums.DiscountType;
import com.T82.coupon.global.domain.repository.CouponEventRepository;
import com.T82.coupon.global.domain.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.T82.coupon.global.domain.enums.Category.SPORTS;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"issueCoupon"})
@Transactional
public class CouponConcurrencyTest {

    @Autowired
    private CouponEventServiceImpl couponEventService;
    @Autowired
    private CouponEventRepository couponEventRepository;
    @Autowired
    private CouponRepository couponRepository;

    private String userId;

    @BeforeEach
    public void setUp() {
        userId = "testUserId";
        UserDto principal = new UserDto(userId, "test@example.com");
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @Transactional
    public void 한명성공() throws InterruptedException {
        // given
        CouponRequestDto couponDto = new CouponRequestDto("테스트쿠폰", DiscountType.PERCENTAGE, 15, Date.from(Instant.parse("2024-12-31T23:59:59.00Z")), 5000, true, SPORTS);
        Coupon savedCoupon = couponRepository.save(couponDto.toEntity(couponDto));
        CouponEvent couponEvent = couponEventRepository.save(new CouponEvent(null, 100, 100, Date.from(Instant.parse("2024-12-31T23:59:59.00Z")), savedCoupon));
        // when
        couponEventService.issueCoupon(String.valueOf(savedCoupon.getCouponId()), userId);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        // Kafka 리스너가 이벤트를 처리할 시간을 기다림
        Thread.sleep(1000);

        // then
        CouponEvent updatedCouponEvent = couponEventRepository.findByCoupon_CouponId(savedCoupon.getCouponId()).orElseThrow();
        assertEquals(99, updatedCouponEvent.getRestCoupon());
    }

    @Test
    @Transactional
    public void 여러명성공() throws InterruptedException {
        // given
        CouponRequestDto couponDto = new CouponRequestDto("테스트쿠폰", DiscountType.PERCENTAGE, 15, Date.from(Instant.parse("2024-12-31T23:59:59.00Z")), 5000, true, SPORTS);
        Coupon savedCoupon = couponRepository.saveAndFlush(couponDto.toEntity(couponDto));
        CouponEvent couponEvent = couponEventRepository.saveAndFlush(new CouponEvent(null, 100, 100, Date.from(Instant.parse("2024-12-31T23:59:59.00Z")), savedCoupon));


        int threadCount = 500;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(()->{
                try{
                    couponEventService.issueCoupon(String.valueOf(savedCoupon.getCouponId()), String.valueOf(userId));
                }finally {
                    latch.countDown();
                }
            });
            TestTransaction.flagForCommit();
            TestTransaction.end();
            TestTransaction.start();
        }
        latch.await();

        Thread.sleep(10000);

        // when
        assertEquals(0,couponEventRepository.findByCoupon_CouponId(savedCoupon.getCouponId()).get().getRestCoupon());

    }
}