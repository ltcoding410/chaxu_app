package com.chaxu.ordering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/** 后续订单接口的输入契约；价格必须从数据库读取，不接收前端传价。 */
public final class OrderContracts {
    private OrderContracts() {}
    public enum Status { PENDING_PAYMENT, PAID, PREPARING, READY, COMPLETED, CANCELLED }
    public record Item(@Positive long productId, @Positive long variantId,
                       @NotBlank String temperature, @NotBlank String sugar,
                       List<@Positive Long> optionIds, @Min(1) @Max(99) int quantity) {}
    public record Create(@NotBlank @Size(max=64) String requestId,
                         @NotEmpty @Size(max=50) List<@Valid Item> items,
                         Long couponId) {}
}
