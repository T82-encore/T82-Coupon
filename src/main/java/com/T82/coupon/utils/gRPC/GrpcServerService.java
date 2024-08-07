package com.T82.coupon.utils.gRPC;

import com.T82.coupon.global.domain.entity.Coupon;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.t82.lib.CouponGrpc;
import org.t82.lib.VerifyReply;
import org.t82.lib.VerifyRequest;

@GrpcService
@RequiredArgsConstructor
public class GrpcServerService extends CouponGrpc.CouponImplBase{
    private final GrpcUtil grpcUtil;

    @Override
    public void verifyCoupons(VerifyRequest request, StreamObserver<VerifyReply> responseObserver) {
        try {
            request.getItemsList().forEach(item -> {
                item.getCouponIdList().forEach(couponId -> {
                    Coupon coupon = grpcUtil.validateIsUsed(request.getUserId(), couponId);
                    grpcUtil.validateExpired(coupon);
                    grpcUtil.validateMinPurchase(item.getBeforeAmount(), coupon);
                });
            });
            responseObserver.onNext(
                    VerifyReply.newBuilder()
                            .setValid(true)
                            .setMessage("쿠폰 검증에 성공하였습니다.")
                            .build()
            );
        } catch (Exception e) {
            responseObserver.onNext(
                    VerifyReply.newBuilder()
                            .setValid(false)
                            .setMessage(e.getMessage())
                    .build()
            );
        }

    }


}
