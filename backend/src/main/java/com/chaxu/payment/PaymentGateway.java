package com.chaxu.payment;

/** 接真实支付前必须实现验签、查单、关单与退款；当前不注册任何模拟成功的实现。 */
public interface PaymentGateway {
    record PaymentStatus(String merchantOrderNo, String transactionId, long paidCents, boolean paid) {}
    PaymentStatus query(String merchantOrderNo);
    void close(String merchantOrderNo);
    void refund(String merchantOrderNo, String refundRequestId, long amountCents);
}
