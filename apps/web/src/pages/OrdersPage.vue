<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Coffee, ArrowRight, Clock3 } from "lucide-vue-next";
import { useShop } from "../stores/shop";
import { useAuth } from "../stores/auth";
const auth = useAuth();
import { money } from "../data/catalog";
const shop = useShop(),
  route = useRoute(),
  router = useRouter();
const filter = computed(() =>
  ["已完成", "进行中", "待取餐"].includes(String(route.query.status))
    ? String(route.query.status)
    : "全部订单",
);
const orders = computed(() =>
  shop.state.orders.filter(
    (o) =>
      filter.value === "全部订单" ||
      (filter.value === "进行中"
        ? o.status !== "已完成"
        : o.status === filter.value),
  ),
);
const datetime = (time: number) =>
  new Intl.DateTimeFormat("zh-CN", {
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(time);
</script>
<template>
  <div class="standard-page">
    <div class="page-heading">
      <p class="eyebrow">你的每一杯好茶</p>
      <h1>我的订单</h1>
      <p>茶香正好，不慌不忙。制作进度在这里查看。</p>
    </div>
    <p v-if="!auth.user" class="guest-notice">登录后查看你的订单，游客可以继续浏览饮品。<RouterLink :to="{ path: '/login', query: { redirect: '/orders' } }">去登录</RouterLink></p>
    <nav class="category-tabs" aria-label="订单状态">
      <RouterLink
        v-for="s in ['全部订单', '进行中', '待取餐', '已完成']"
        :key="s"
        :to="{ query: { status: s === '全部订单' ? undefined : s } }"
        :class="{ selected: filter === s }"
        >{{ s }}</RouterLink
      >
    </nav>
    <section class="white-section empty-state" v-if="!orders.length">
      <Coffee :size="42" :stroke-width="1.3" aria-hidden="true" />
      <h2>
        {{
          shop.state.orders.length
            ? "这里还没有对应的订单"
            : "第一杯好茶，正等着你"
        }}
      </h2>
      <p>点一杯喜欢的茶，开始今天的好心情。</p>
      <RouterLink to="/" class="button primary"
        >去选一杯<ArrowRight :size="16" aria-hidden="true"
      /></RouterLink>
    </section>
    <article class="order-card" v-for="order in orders" :key="order.id">
      <header>
        <div>
          <h2>茶序 · 城市花园店</h2>
          <p>{{ datetime(order.time) }} · 到店自取</p>
        </div>
        <span class="soft-tag">{{ order.status }}</span>
      </header>
      <div class="order-body">
        <div class="pickup-number">
          <span>取餐码</span><strong>{{ order.pickup }}</strong
          ><small
            ><Clock3 :size="13" aria-hidden="true" />{{
              order.status === "待取餐"
                ? "好茶已备好，请到店取餐"
                : order.status === "已完成"
                  ? "感谢你的喜欢"
                  : "预计 10–15 分钟"
            }}</small
          >
        </div>
        <div class="order-items">
          <div v-for="(line, index) in order.lines" :key="index">
            <div>
              <h3>
                {{ line.name }} <span>× {{ line.quantity }}</span>
              </h3>
              <p>{{ line.spec }}</p>
            </div>
            <strong>{{ money(line.price * line.quantity) }}</strong>
          </div>
        </div>
      </div>
      <footer>
        <span
          >本次获得 <strong>{{ order.earned }}</strong> 积分 ·
          本地模拟订单</span
        >
        <div>
          <span v-if="order.discount">已优惠 {{ money(order.discount) }}</span
          >实付 <strong>{{ money(order.paid) }}</strong
          ><button
            class="text-button"
            @click="
              order.lines.forEach((l) =>
                shop.add({
                  key: l.key,
                  name: l.name,
                  productId: l.productId,
                  price: l.price,
                  spec: l.spec,
                }),
              );
              router.push('/');
            "
          >
            再来一单
          </button>
        </div>
      </footer>
    </article>
  </div>
</template>
