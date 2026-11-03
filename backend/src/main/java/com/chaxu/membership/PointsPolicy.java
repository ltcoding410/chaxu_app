package com.chaxu.membership;

/** 金额单位为分，积分取整；正式入账需位于支付确认的同一个本地事务中。 */
public final class PointsPolicy {
    private PointsPolicy() {}
    public static long earned(long paidCents) {
        if (paidCents < 0) throw new IllegalArgumentException("实付金额不能为负");
        return paidCents / 100;
    }
}
