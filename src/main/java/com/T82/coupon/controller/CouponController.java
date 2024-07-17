package com.T82.coupon.controller;

import com.T82.coupon.dto.request.CreateCouponRequestDto;
import com.T82.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CouponController {
    private final CouponService couponService;

    @PostMapping("/coupons")
    @ResponseStatus(HttpStatus.OK)
    public void createCoupon(@RequestBody CreateCouponRequestDto req){
        couponService.createCoupon(req);
    };

}
