package com.T82.coupon.global.producer;

import com.T82.coupon.global.domain.dto.IssueCouponDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponIssueProducer {
    private final KafkaTemplate<String, IssueCouponDto> kafkaTemplate;
    public void issueCoupon(IssueCouponDto req){
        kafkaTemplate.send("issueCoupon",req);
    }
}
