package com.T82.coupon.controller;

import com.T82.coupon.dto.request.CouponEventRequestDto;
import com.T82.coupon.dto.request.CouponRequestDto;
import com.T82.coupon.dto.request.CouponVerifyRequestDto;
import com.T82.coupon.dto.response.CouponResponseDto;
import com.T82.coupon.dto.response.CouponVerifyResponseDto;
import com.T82.coupon.global.domain.dto.UserDto;
import com.T82.coupon.service.CouponEventService;
import com.T82.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {
    private final CouponService couponService;
    private final CouponEventService couponEventService;

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
    public void giveCouponToUser(@AuthenticationPrincipal UserDto userDto, @PathVariable(name = "couponId") String couponId){
        couponService.giveCouponToUser(couponId,userDto.getId());
    }
    /**
     * 유저가 보유한 사용기한이 지나지 않은 쿠폰 내역 가져오기(10개)
     */
    @GetMapping("/valid")
    @ResponseStatus(HttpStatus.OK)
    public Page<CouponResponseDto> getValidCoupons(@AuthenticationPrincipal UserDto userDto,@PageableDefault(size =5, page = 0,sort = "validEnd", direction = Sort.Direction.ASC) Pageable pageRequest){
        return couponService.getValidCoupons(pageRequest,userDto);
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
    /**
     * 사용가능한 쿠폰인지 검증 (결제 서비스에서 호출)
     */
//    @PostMapping("/verify")
//    @ResponseStatus(HttpStatus.OK)
//    public CouponVerifyResponseDto verifyCoupons(@RequestBody CouponVerifyRequestDto req) {
//        return couponService.verifyCoupons(req);
//    }

    /**
     * 쿠폰 이벤트 생성 (쿠폰+쿠폰이벤트 생성)
     */
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public void createCouponEvent(@RequestBody CouponEventRequestDto req) {
        couponEventService.createCouponEvent(req);
    }

    /**
     * 이벤트 쿠폰 발급
     */
    @PostMapping("/events/issue")
    @ResponseStatus(HttpStatus.OK)
    public void issueCoupon(@AuthenticationPrincipal UserDto userDto, @RequestBody Map<String, String> req) {
        couponEventService.issueCoupon(req.get("couponId"), userDto.getId());
    }
}
