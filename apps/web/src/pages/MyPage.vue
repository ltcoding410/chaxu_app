<script setup lang="ts">
import { computed } from "vue";
import {
  UserRound,
  ChevronRight,
  Gift,
  Ticket,
  Coins,
  Coffee,
  Clock3,
  ShoppingBag,
  CheckCheck,
  MapPin,
  Info,
} from "lucide-vue-next";
import { useShop } from "../stores/shop";
import { useAuth } from "../stores/auth";
import LogoutButton from "../components/LogoutButton.vue";
const auth = useAuth();
const shop = useShop();
const pending = computed(
  () => shop.state.orders.filter((o) => o.status !== "已完成").length,
);
const ready = computed(
  () => shop.state.orders.filter((o) => o.status === "待取餐").length,
);
const complete = computed(
  () => shop.state.orders.filter((o) => o.status === "已完成").length,
);
</script>
<template>
  <div class="standard-page my-page">
    <div class="page-heading">
      <p class="eyebrow">你的好茶日常</p>
      <h1>我的</h1>
      <p>每一杯喜欢，都值得好好收藏。</p>
    </div>
    <section class="my-profile" aria-labelledby="profile-title">
      <div class="profile-identity">
        <span class="profile-avatar"
          ><UserRound :size="28" aria-hidden="true"
        /></span>
        <div>
          <h2 id="profile-title">{{ auth.user?.displayName || '欢迎来到茶序' }}</h2>
          <p>{{ auth.user ? `账号：${auth.user.username}` : '登录后可购物，随时可以先逛逛。' }}</p>
        </div>
        <LogoutButton v-if="auth.user" />
        <RouterLink v-else to="/login" class="button primary">登录 / 注册</RouterLink>
      </div>
      <div class="my-assets">
        <RouterLink to="/member#points"
          ><strong>{{ shop.state.points }}</strong
          ><span>我的积分<ChevronRight :size="13" aria-hidden="true" /></span
        ></RouterLink>
        <RouterLink to="/member#coupons"
          ><strong>{{ shop.availableCoupons.length }}<small>张</small></strong
          ><span>优惠券<ChevronRight :size="13" aria-hidden="true" /></span
        ></RouterLink>
        <RouterLink to="/orders"
          ><strong>{{ shop.state.orders.length }}<small>单</small></strong
          ><span>累计订单<ChevronRight :size="13" aria-hidden="true" /></span
        ></RouterLink>
      </div>
    </section>
    <section class="white-section my-orders" aria-labelledby="my-orders-title">
      <div class="section-heading">
        <h2 id="my-orders-title">我的订单</h2>
        <RouterLink to="/orders" class="inline-link"
          >全部订单<ChevronRight :size="15" aria-hidden="true"
        /></RouterLink>
      </div>
      <div class="order-shortcuts">
        <RouterLink to="/orders?status=进行中"
          ><Clock3 :size="24" aria-hidden="true" /><span>制作与取餐</span
          ><small>{{ pending }} 单进行中</small></RouterLink
        >
        <RouterLink to="/orders?status=待取餐"
          ><ShoppingBag :size="24" aria-hidden="true" /><span>待取茶</span
          ><small>{{ ready }} 单已备好</small></RouterLink
        >
        <RouterLink to="/orders?status=已完成"
          ><CheckCheck :size="24" aria-hidden="true" /><span>已完成</span
          ><small>{{ complete }} 单好茶时光</small></RouterLink
        >
      </div>
    </section>
    <RouterLink to="/member" class="my-reward-banner"
      ><span class="reward-icon"><Gift :size="27" aria-hidden="true" /></span>
      <div>
        <h2>把喜欢，攒成小确幸。</h2>
        <p>100 积分可兑换满 20 减 5 元优惠券</p>
      </div>
      <span class="reward-action"
        >积分兑好礼<ChevronRight :size="17" aria-hidden="true" /></span
    ></RouterLink>
    <section class="white-section my-services" aria-labelledby="services-title">
      <div class="section-heading"><h2 id="services-title">我的服务</h2></div>
      <RouterLink to="/member#points" class="service-row"
        ><Coins :size="20" aria-hidden="true" /><span>积分明细</span
        ><ChevronRight :size="17" aria-hidden="true"
      /></RouterLink>
      <RouterLink to="/member#coupons" class="service-row"
        ><Ticket :size="20" aria-hidden="true" /><span>我的优惠券</span
        ><ChevronRight :size="17" aria-hidden="true"
      /></RouterLink>
      <details class="service-details">
        <summary>
          <MapPin :size="20" aria-hidden="true" /><span>门店与取餐说明</span
          ><ChevronRight :size="17" aria-hidden="true" />
        </summary>
        <div>
          <p>茶序 · 城市花园店，营业时间 10:00–22:00。</p>
          <p>
            下单后可在“我的订单”查看取餐码与进度，状态为“待取餐”时请凭取餐码到店取茶。
          </p>
        </div>
      </details>
      <details class="service-details">
        <summary>
          <Info :size="20" aria-hidden="true" /><span>会员与体验说明</span
          ><ChevronRight :size="17" aria-hidden="true" />
        </summary>
        <div>
          <p>
            注册账号保存在服务器，密码经加密哈希存储。当前订单、积分和券仍是按账号隔离的浏览器体验数据，不会产生真实扣款。
          </p>
          <p>
            实付每满 1 元得 1 积分；兑换券 30
            天内有效，一单一券。清理浏览器数据会移除体验记录。
          </p>
        </div>
      </details>
    </section>
    <p class="my-footnote">
      <Coffee :size="14" aria-hidden="true" />茶序，陪你认真过好每一天。
    </p>
  </div>
</template>
