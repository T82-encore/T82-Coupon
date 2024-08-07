package com.T82.coupon.global.producer;

import com.T82.coupon.global.domain.dto.IssueCouponDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponIssueProducer {
    private final KafkaTemplate<String, IssueCouponDto> kafkaTemplate;
    public void issueCoupon(IssueCouponDto req){
        log.error("before kafka {}",req.toString());
        kafkaTemplate.send("issueCoupon",req);
    }
}
