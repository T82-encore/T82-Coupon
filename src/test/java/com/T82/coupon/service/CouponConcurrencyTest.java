package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponEventRequestDto;
import com.T82.coupon.global.domain.dto.IssueCouponDto;
import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.entity.CouponBox;
import com.T82.coupon.global.domain.entity.CouponEvent;
import com.T82.coupon.global.domain.repository.CouponBoxRepository;
import com.T82.coupon.global.domain.repository.CouponEventRepository;
import com.T82.coupon.global.domain.repository.CouponRepository;
import com.T82.coupon.global.producer.CouponIssueProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CouponConcurrencyTest {

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponBoxRepository couponBoxRepository;

    @Mock
    private CouponService couponService;

    @Mock
    private CouponIssueProducer couponIssueProducer;

    @InjectMocks
    private CouponEventServiceImpl couponEventServiceImpl;

    private CouponEvent couponEvent;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        coupon = Coupon.builder()
                .couponId(UUID.randomUUID())
                .build();

        couponEvent = CouponEvent.builder()
                .coupon(coupon)
                .totalCoupon(10)
                .restCoupon(10)
                .eventStartTime(new Date())
                .build();
    }

    @Test
    void createCouponEvent() {
        CouponEventRequestDto requestDto = mock(CouponEventRequestDto.class);
        when(requestDto.toCouponEntity(any())).thenReturn(coupon);
        when(requestDto.toCouponEventEntity(any(), any())).thenReturn(couponEvent);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);
        when(couponEventRepository.save(any(CouponEvent.class))).thenReturn(couponEvent);

        couponEventServiceImpl.createCouponEvent(requestDto);

        verify(couponRepository).save(any(Coupon.class));
        verify(couponEventRepository).save(any(CouponEvent.class));
    }

    @Test
    void issueCoupon() {
        String couponId = coupon.getCouponId().toString();
        String userId = "user123";

        when(couponEventRepository.findByCoupon_CouponId(any(UUID.class)))
                .thenReturn(Optional.of(couponEvent));
        when(couponBoxRepository.findByCouponIdAndUserId(any(UUID.class), anyString()))
                .thenReturn(Optional.of(new CouponBox()));

        couponEventServiceImpl.issueCoupon(couponId, userId);

        verify(couponIssueProducer).issueCoupon(any(IssueCouponDto.class));
    }

    @Test
    void issueCouponFromEvent() {
        String couponId = coupon.getCouponId().toString();
        String userId = "user123";

        IssueCouponDto issueCouponDto = IssueCouponDto.toDto(couponId, userId);

        // Mock CouponEvent repository to return a CouponEvent object
        CouponEvent couponEventMock = CouponEvent.builder()
                .coupon(coupon)
                .totalCoupon(10)
                .restCoupon(1) // Set to 1 to allow decrement
                .eventStartTime(new Date())
                .build();
        when(couponEventRepository.findByCoupon_CouponId(any(UUID.class)))
                .thenReturn(Optional.of(couponEventMock));

        // Mock CouponService to do nothing
        doNothing().when(couponService).giveCouponToUser(anyString(), anyString());

        couponEventServiceImpl.issueCouponFromEvent(issueCouponDto);

        // Verify that CouponService was called
        verify(couponService).giveCouponToUser(anyString(), anyString());

        // Verify that the CouponEvent was updated
        verify(couponEventRepository).findByCoupon_CouponId(any(UUID.class));
    }
}