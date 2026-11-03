<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import {
  ShoppingBag,
  Coins,
  Coffee,
  ArrowRight,
  Check,
  CircleHelp,
} from "lucide-vue-next";
import { useShop } from "../stores/shop";
import { useAuth } from "../stores/auth";
const auth = useAuth();
import { money, products } from "../data/catalog";
const shop = useShop(),
  route = useRoute();
const tab = computed(() =>
  route.query.tab === "products" ? "products" : "orders",
);
const revenue = computed(() =>
  shop.state.orders.reduce((sum, o) => sum + o.paid, 0),
);
const active = computed(() =>
  shop.state.orders.filter((o) => o.status !== "已完成"),
);
const nextLabel = {
  待接单: "接单并制作",
  制作中: "制作完成",
  待取餐: "确认已取餐",
  已完成: "已完成",
};
</script>
<template>
  <div class="standard-page">
    <div class="page-heading admin-heading">
      <div>
        <p class="eyebrow">把每一杯，认真做好</p>
        <h1>门店工作台</h1>
        <p>城市花园店 · 今天也是茶香满满的一天。</p>
      </div>
      <span class="soft-tag"><span class="status-dot"></span>营业中</span>
    </div>
    <div class="admin-demo-note">
      <CircleHelp :size="17" aria-hidden="true" /><span
        >{{ auth.isStaff ? '已验证店员身份。当前订单和商品状态仍为本账号的本地模拟数据。' : '当前为只读浏览，处理订单与管理商品需要店员权限。' }}</span
      >
    </div>
    <div class="stats-grid">
      <article>
        <span>体验订单实付合计</span><strong>{{ money(revenue) }}</strong
        ><Coins :size="22" aria-hidden="true" />
      </article>
      <article>
        <span>体验订单</span
        ><strong>{{ shop.state.orders.length }}<small>单</small></strong
        ><ShoppingBag :size="22" aria-hidden="true" />
      </article>
      <article>
        <span>待处理订单</span
        ><strong>{{ active.length }}<small>单</small></strong
        ><Coffee :size="22" aria-hidden="true" />
      </article>
    </div>
    <nav class="category-tabs" aria-label="后台功能">
      <RouterLink to="/admin" :class="{ selected: tab === 'orders' }"
        >订单处理</RouterLink
      ><RouterLink
        to="/admin?tab=products"
        :class="{ selected: tab === 'products' }"
        >商品管理</RouterLink
      >
    </nav>
    <section v-if="tab === 'orders'" class="white-section">
      <div class="section-heading">
        <h2>订单看板</h2>
        <span class="muted">接单 → 制作 → 取餐</span>
      </div>
      <div class="empty-state" v-if="!shop.state.orders.length">
        <Coffee :size="34" aria-hidden="true" />
        <h3>还没有需要处理的订单</h3>
        <p>从顾客端完成一次模拟下单，就能在这里接单。</p>
        <RouterLink to="/" class="button secondary"
          >前往顾客端<ArrowRight :size="16" aria-hidden="true"
        /></RouterLink>
      </div>
      <div class="table-scroll" v-else>
        <table>
          <caption class="sr-only">
            本地体验订单
          </caption>
          <thead>
            <tr>
              <th scope="col">取餐码 / 饮品</th>
              <th scope="col">实付金额</th>
              <th scope="col">订单状态</th>
              <th scope="col">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in shop.state.orders" :key="o.id">
              <td>
                <strong>{{ o.pickup }}</strong>
                <p>
                  {{
                    o.lines.map((l) => `${l.name} ×${l.quantity}`).join("、")
                  }}
                </p>
              </td>
              <td>{{ money(o.paid) }}</td>
              <td>
                <span class="soft-tag">{{ o.status }}</span>
              </td>
              <td>
                <button
                  class="button secondary"
                  :disabled="!auth.isStaff || o.status === '已完成'"
                  @click="shop.advance(o.id)"
                >
                  {{ nextLabel[o.status]
                  }}<Check
                    v-if="o.status === '已完成'"
                    :size="15"
                    aria-hidden="true"
                  /><ArrowRight v-else :size="15" aria-hidden="true" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
    <section v-else class="white-section">
      <div class="section-heading">
        <h2>饮品在售管理</h2>
        <span class="muted"
          >{{ products.length - shop.state.unavailable.length }} 款在售</span
        >
      </div>
      <div class="table-scroll">
        <table>
          <caption class="sr-only">
            商品上下架管理
          </caption>
          <thead>
            <tr>
              <th scope="col">饮品名称</th>
              <th scope="col">分类</th>
              <th scope="col">起售价</th>
              <th scope="col">售卖状态</th>
              <th scope="col">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in products" :key="p.id">
              <td>
                <strong>{{ p.name }}</strong>
                <p>{{ p.english }}</p>
              </td>
              <td>{{ p.category }}</td>
              <td>{{ money(p.price) }}</td>
              <td>
                {{ shop.state.unavailable.includes(p.id) ? "已售罄" : "在售" }}
              </td>
              <td>
                <button
                  class="button secondary"
                  :disabled="!auth.isStaff"
                  @click="shop.toggleProduct(p.id)"
                >
                  {{
                    shop.state.unavailable.includes(p.id)
                      ? "恢复在售"
                      : "标记售罄"
                  }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
