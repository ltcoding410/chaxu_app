package com.chaxu.coupon;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class CouponContracts {
    private CouponContracts() {}
    public enum Status { AVAILABLE, LOCKED, USED, EXPIRED }
    public record Redeem(@NotBlank @Size(max=64) String requestId, @Positive long templateId) {}
}
