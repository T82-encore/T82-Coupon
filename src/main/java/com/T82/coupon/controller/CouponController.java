package com.T82.coupon.controller;

import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CouponController {
    private final CouponService couponService;

    /**
     * 쿠폰 생성
     */
    @PostMapping("/coupons")
    @ResponseStatus(HttpStatus.OK)
    public void createCoupon(@RequestBody CouponRequestDto req){
        couponService.createCoupon(req);
    };

    /**
     * 카테고리별로 쿠폰 가져오기(페이징 5개씩)
     */
    @GetMapping("/coupons")
    public Page<CouponResponseDto> getCouponsByCategory(@RequestParam(value = "category", required = false) Category category,
                                                    @PageableDefault(size =5, page = 0,sort = "validEnd", direction = Sort.Direction.ASC) Pageable pageRequest){
        return couponService.getCouponsByCategory(category,pageRequest);
    }

}
