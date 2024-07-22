package com.T82.coupon.utils;

import com.T82.coupon.global.domain.entity.Coupon;
import com.T82.coupon.global.domain.enums.Category;
import com.T82.coupon.global.domain.exception.CategoryNotMatchException;
import com.T82.coupon.global.domain.exception.DuplicateCouponException;
import com.T82.coupon.global.domain.exception.MinPurchaseException;
import org.springframework.stereotype.Component;


@Component
public class CouponValidateUtil {
    public static void validateCategory(Category category, Coupon coupon) {
        if (!coupon.getCategory().equals(category) && !coupon.getCategory().equals(Category.ALL)) {
            throw new CategoryNotMatchException();
        }
    }

    public static void validateMinPurchase(int amount, Coupon coupon) {
        if (coupon.getMinPurchase() > amount) {
            throw new MinPurchaseException();
        }
    }

    public static boolean validateNonDuplicateCoupon(boolean hasNonDuplicateCoupon, Coupon coupon) {
        if (!coupon.getDuplicate()) {
            if (hasNonDuplicateCoupon) {
                throw new DuplicateCouponException();
            }
            hasNonDuplicateCoupon = true;
        }
        return hasNonDuplicateCoupon;
    }
}