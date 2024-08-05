package com.T82.coupon.service;

import com.T82.common_exception.annotation.CustomException;
import com.T82.common_exception.exception.ErrorCode;
import com.T82.coupon.dto.request.CouponEventRequestDto;
import com.T82.coupon.global.domain.dto.IssueCouponDto;
import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.entity.CouponEvent;
import com.T82.coupon.global.domain.repository.CouponBoxRepository;
import com.T82.coupon.global.domain.repository.CouponEventRepository;
import com.T82.coupon.global.domain.repository.CouponRepository;
import com.T82.coupon.global.producer.CouponIssueProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponEventServiceImpl implements CouponEventService{
    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final CouponBoxRepository couponBoxRepository;
    private final CouponService couponService;
    private final CouponIssueProducer couponIssueProducer;

    /**
     * 쿠폰 이벤트 생성 (쿠폰+이벤트 생성)
     */
    @Override
    @Transactional
    public void createCouponEvent(CouponEventRequestDto req) {
        Coupon savedCoupon = couponRepository.save(req.toCouponEntity(req));
        couponEventRepository.save(req.toCouponEventEntity(req,savedCoupon));
    }

    /**
     * 쿠폰 이벤트에서 쿠폰 발급 실제 로직(To Kafka)
     */
    @Override
    @Transactional
    @CustomException(ErrorCode.COUPON_NOT_FOUND)
    public void issueCoupon(String couponId,String userId){
        CouponEvent byCouponId = couponEventRepository.findByCoupon_CouponId(UUID.fromString(couponId)).orElseThrow(IllegalArgumentException::new);
        if(couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(couponId),userId).isPresent()) throw new IllegalArgumentException();
        if(byCouponId.getRestCoupon()<=0) throw new IllegalArgumentException();
        couponIssueProducer.issueCoupon(IssueCouponDto.toDto(couponId,userId));
    }

    /**
     * 쿠폰 이벤트에서 쿠폰 발급 (From Kafka)
     */
    @KafkaListener(topics = "issueCoupon")
    @Transactional
    @CustomException(ErrorCode.COUPON_VALIDATE_FAILED)
    public void issueCouponFromEvent(IssueCouponDto req) {
        couponService.giveCouponToUser(req.couponId(),req.userId());
        CouponEvent byCouponCouponId = couponEventRepository.findByCoupon_CouponId(UUID.fromString(req.couponId())).orElseThrow(IllegalArgumentException::new);
        if (byCouponCouponId.getRestCoupon()<=0) throw new IllegalArgumentException();
        byCouponCouponId.subRestCoupon();// 남은 쿠폰 1차감
    }
}
