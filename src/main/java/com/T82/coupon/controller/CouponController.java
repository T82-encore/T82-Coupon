package com.T82.coupon.controller;

import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
import com.T82.coupon.global.domain.dto.UserDto;
import com.T82.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {
    private final CouponService couponService;

    /**
     * 쿠폰 생성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void createCoupon(@RequestBody CouponRequestDto req){
        couponService.createCoupon(req);
    };

    /**
     * 유저의 쿠폰함에 쿠폰 지급
     */
    @PostMapping("/{couponId}")
    @ResponseStatus(HttpStatus.OK)
    public void giveCouponToUser(@PathVariable(name = "couponId") String couponId){
        UserDto principal = (UserDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        couponService.giveCouponToUser(couponId,principal.getId());
    }
    /**
     * 유저가 보유한 사용기한이 지나지 않은 쿠폰 내역 가져오기(10개)
     */
    @GetMapping("/valid")
    @ResponseStatus(HttpStatus.OK)
    public Page<CouponResponseDto> getValidCoupons(@PageableDefault(size =5, page = 0,sort = "validEnd", direction = Sort.Direction.ASC) Pageable pageRequest){
        return couponService.getValidCoupons(pageRequest);
    }
    /**
     * 카테고리별로 쿠폰 가져오기(페이징 5개씩)
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<CouponResponseDto> getCouponsByCategory(@RequestParam(value = "category", required = false) String category,
                                                        @PageableDefault(size =5, page = 0,sort = "validEnd", direction = Sort.Direction.ASC) Pageable pageRequest){
        return couponService.getCouponsByCategory(category,pageRequest);
    }
}
