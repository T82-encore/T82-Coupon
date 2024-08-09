package com.T82.coupon.service;

import com.T82.common_exception.annotation.CustomException;
import com.T82.common_exception.annotation.ExecutionTimeLog;
import com.T82.common_exception.exception.ErrorCode;
import com.T82.common_exception.exception.coupon.*;
import com.T82.coupon.dto.request.CouponEventRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
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
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.kafka.retrytopic.TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE;

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
    @CustomException(ErrorCode.FAILED_GENERATE_COUPON)
    public void createCouponEvent(CouponEventRequestDto req) {
        Coupon savedCoupon = couponRepository.save(req.toCouponEntity(req));
        couponEventRepository.save(req.toCouponEventEntity(req,savedCoupon));
    }
    /**
     * 쿠폰 이벤트에서 쿠폰 발급 실제 로직(To Kafka)
     */
    @Override
    @Transactional
    @CustomException(ErrorCode.FAILED_ISSUE_COUPON)
    public void issueCoupon(String couponId,String userId){
        CouponEvent byCouponId = couponEventRepository.findByCoupon_CouponId(UUID.fromString(couponId)).orElseThrow(NotFoundEventCouponException::new);
        if(couponBoxRepository.findByCouponIdAndUserId(UUID.fromString(couponId),userId).isPresent()) throw new CouponAlreadyIssuedException(); //중복 발급 불가
        if(byCouponId.getRestCoupon()<=0) throw new NotFoundRemainingCouponException();
        couponIssueProducer.issueCoupon(IssueCouponDto.toDto(couponId,userId));
    }
    /**
     * 쿠폰 이벤트에서 쿠폰 발급 (From Kafka)
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 10 * 1000, multiplier = 3, maxDelay = 10 * 60 * 1000),
            topicSuffixingStrategy = SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR,
            include = FailedIssueCouponException.class
    )
    @KafkaListener(topics = "issueCoupon", groupId = "eventCoupon-group")
    @Transactional
    @CustomException(ErrorCode.FAILED_ISSUE_COUPON)
    public void issueCouponFromEvent(IssueCouponDto req) {
        couponService.giveCouponToUser(req.couponId(),req.userId());
        CouponEvent byCouponCouponId = couponEventRepository.findByCoupon_CouponId(UUID.fromString(req.couponId())).orElseThrow(CouponNotFoundException::new); //쿠폰존재x
        if (byCouponCouponId.getRestCoupon()<=0) throw new NotFoundRemainingCouponException();
        byCouponCouponId.subRestCoupon();// 남은 쿠폰 1차감
    }
    /**
     * 이벤트 진행중인 쿠폰들 반환
     */
    @Transactional
    @CustomException(ErrorCode.NOT_FOUND_EVENT_COUPON)
    public List<CouponResponseDto> getEventCoupons() {
        List<Coupon> eventCoupons = couponRepository.findEventCoupons();
        return eventCoupons.stream().map(CouponResponseDto::from).toList();
    }
}
