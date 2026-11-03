<script setup lang="ts">
import { computed } from "vue";
import { Leaf, ArrowRight, Ticket, Gift } from "lucide-vue-next";
import { useShop } from "../stores/shop";
import { useAuth } from "../stores/auth";
const auth = useAuth();
import { templates, money } from "../data/catalog";
const shop = useShop();
const date = (time: number) =>
  new Intl.DateTimeFormat("zh-CN", { month: "2-digit", day: "2-digit" }).format(
    time,
  );
const coupons = computed(() =>
  shop.state.coupons.map((c) => ({
    ...c,
    template: templates.find((t) => t.id === c.templateId)!,
  })),
);
</script>
<template>
  <div class="standard-page">
    <div class="page-heading">
      <p class="eyebrow">每一杯，都算数</p>
      <h1>我的茶序时光</h1>
      <p>把日常的小喜欢，攒成下一次的小确幸。</p>
    </div>
    <p v-if="!auth.user" class="guest-notice">游客可以查看会员权益，<RouterLink :to="{ path: '/login', query: { redirect: '/member' } }">登录后兑换好礼</RouterLink>。</p>
    <section class="member-banner">
      <div>
        <div class="member-welcome">
          <Leaf :size="23" aria-hidden="true" />茶序体验会员
        </div>
        <h2>好茶常伴，心意常在。</h2>
        <p>实付每满 1 元积 1 分，让喜欢多一点回馈。</p>
      </div>
      <div class="member-points">
        <span>可用积分</span><strong>{{ shop.state.points }}</strong
        ><span>我的优惠券 {{ shop.availableCoupons.length }} 张</span>
      </div>
    </section>
    <section>
      <div class="section-heading">
        <div>
          <p class="eyebrow">一份积分，一份心意</p>
          <h2>积分兑好礼</h2>
        </div>
        <Gift :size="23" aria-hidden="true" />
      </div>
      <div class="coupon-grid">
        <article v-for="t in templates" :key="t.id" class="coupon-card">
          <div class="coupon-amount">
            <small>¥</small>{{ t.discount / 100
            }}<span>{{ t.description }}</span>
          </div>
          <div class="coupon-content">
            <h3>{{ t.name }}</h3>
            <p>兑换后 30 天内有效 · 每单限用一张</p>
            <div>
              <strong>{{ t.cost }} <small>积分</small></strong
              ><button
                class="button secondary"
                :disabled="
                  !!auth.user && (shop.state.points < t.cost ||
                  shop.state.coupons.filter((c) => c.templateId === t.id)
                    .length >= 3)
                "
                @click="shop.redeem(t.id)"
              >
                {{
                  !auth.user ? "登录后兑换" : shop.state.coupons.filter((c) => c.templateId === t.id)
                    .length >= 3
                    ? "已达兑换上限"
                    : shop.state.points < t.cost
                      ? "积分不足"
                      : "立即兑换"
                }}<ArrowRight :size="15" aria-hidden="true" />
              </button>
            </div>
          </div>
        </article>
      </div>
      <p class="section-note">
        每种券最多兑换 3 张；兑换后不退积分，券过期不返还。以上为本地体验规则。
      </p>
    </section>
    <div class="member-detail-grid">
      <section class="white-section" id="coupons">
        <div class="section-heading">
          <h2>我的券包</h2>
          <Ticket :size="20" aria-hidden="true" />
        </div>
        <div v-if="!coupons.length" class="empty-state compact">
          <Ticket :size="30" aria-hidden="true" />
          <h3>下一杯的优惠，在这里等你</h3>
          <p>用上方积分兑换一张优惠券吧。</p>
        </div>
        <article class="wallet-row" v-for="c in coupons" :key="c.id">
          <span class="wallet-value">{{ money(c.template.discount) }}</span>
          <div>
            <h3>{{ c.template.name }}</h3>
            <p>{{ c.template.description }} · {{ date(c.expiresAt) }} 到期</p>
          </div>
          <span class="soft-tag">{{
            c.used ? "已使用" : c.expiresAt <= Date.now() ? "已过期" : "可使用"
          }}</span>
        </article>
      </section>
      <section class="white-section" id="points">
        <div class="section-heading">
          <h2>积分足迹</h2>
          <span class="muted">{{ shop.state.ledger.length }} 笔</span>
        </div>
        <div class="ledger-row" v-for="l in shop.state.ledger" :key="l.id">
          <div>
            <h3>{{ l.label }}</h3>
            <p>{{ date(l.time) }}</p>
          </div>
          <strong :class="{ positive: l.amount > 0 }"
            >{{ l.amount > 0 ? "+" : "" }}{{ l.amount }}</strong
          >
        </div>
      </section>
    </div>
  </div>
</template>
