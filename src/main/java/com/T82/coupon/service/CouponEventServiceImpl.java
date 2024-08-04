package com.T82.coupon.service;

import com.T82.coupon.dto.request.CouponEventRequestDto;
import com.T82.coupon.global.domain.dto.IssueCouponDto;
import com.T82.coupon.global.domain.dto.UserDto;
import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.repository.CouponEventRepository;
import com.T82.coupon.global.domain.repository.CouponRepository;
import com.T82.coupon.global.producer.CouponIssueProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponEventServiceImpl implements CouponEventService{
    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
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
     * 쿠폰 이벤트에서 쿠폰 발급 (To Kafka)
     */
    @Override
    @Transactional
    public void issueCoupon(String couponId,String userId){
        couponIssueProducer.issueCoupon(IssueCouponDto.toDto(couponId,userId));
    }

    /**
     * 쿠폰 이벤트에서 쿠폰 발급 (From Kafka)
     */
    @KafkaListener(topics = "issueCoupon")
    @Transactional
    public void issueCouponFromEvent(IssueCouponDto req) {
        couponService.giveCouponToUser(req.couponId(),req.userId());
    }

}
