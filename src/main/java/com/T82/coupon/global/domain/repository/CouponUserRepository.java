package com.T82.coupon.global.domain.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponUserRepository {
    private final RedisTemplate<String, String> redisTemplate;
    public Long add(String userId){
        return redisTemplate
                .opsForSet()
                .add("coupon_user",userId);
    }
 }
