<script setup lang="ts">
import { computed, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  Leaf,
  MapPin,
  Coffee,
  ClipboardList,
  UserRound,
  LayoutDashboard,
  Package,
  LogOut,
  ShieldCheck,
} from "lucide-vue-next";
import { useShop } from "../stores/shop";
import { useAuth } from "../stores/auth";
import LogoutButton from "./LogoutButton.vue";
const auth = useAuth();
const canManageStore = computed(() => auth.isStaff);
const shop = useShop(),
  route = useRoute(),
  router = useRouter();
const staffArea = computed(() => route.meta.staffArea === true);
const customerLinks = [
  { to: "/", label: "在线点单", icon: Coffee },
  { to: "/orders", label: "我的订单", icon: ClipboardList },
  { to: "/me", label: "我的", icon: UserRound },
];
const staffLinks = [
  { to: "/admin", label: "订单处理", icon: LayoutDashboard },
  { to: "/admin?tab=products", label: "商品管理", icon: Package },
];
const links = computed(() =>
  staffArea.value ? staffLinks : customerLinks,
);
watch(() => auth.loginNeeded, () => {
  shop.tell("请先登录，再继续购物。");
  router.push({ path: "/login", query: { redirect: route.fullPath } });
});
function active(to: string) {
  if (to === "/me") return ["/me", "/member"].includes(route.path);
  if (to.startsWith("/admin"))
    return (
      route.path === "/admin" &&
      (to.includes("products")
        ? route.query.tab === "products"
        : route.query.tab !== "products")
    );
  return route.path === to;
}
function leaveStaff() {
  router.replace("/");
}
</script>
<template>
  <a class="skip-link" href="#main-content">跳到主要内容</a>
  <div class="app-shell" :class="{ 'staff-shell': staffArea }">
    <aside class="sidebar">
      <RouterLink
        :to="staffArea ? '/staff/login' : '/'"
        class="brand"
        :aria-label="staffArea ? '茶序店员端' : '茶序首页'"
      >
        <span class="brand-symbol"><Leaf :size="28" aria-hidden="true" /></span
        ><span>茶序<small>CHAXU TEA</small></span>
      </RouterLink>
      <p class="sidebar-caption">
        {{ staffArea ? "门店管理 · 店员专属" : "一杯好茶，一点日常。" }}
      </p>
      <nav class="main-nav" :aria-label="staffArea ? '门店导航' : '主导航'">
        <RouterLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          :class="{ 'nav-selected': active(link.to) }"
          :aria-current="active(link.to) ? 'page' : undefined"
        >
          <component :is="link.icon" :size="19" aria-hidden="true" />{{
            link.label
          }}
          <span
            v-if="
              !staffArea && link.to === '/orders' && shop.state.orders.length
            "
            class="nav-count"
            >{{ shop.state.orders.length }}</span
          >
        </RouterLink>
      </nav>
      <div class="sidebar-bottom">
        <div class="store-note">
          <span class="status-dot"></span>好茶正在营业中<small
            >每日 10:00 — 22:00</small
          >
        </div>
        <button
          v-if="staffArea && canManageStore"
          class="staff-exit text-button"
          @click="leaveStaff"
        >
          <LogOut :size="17" aria-hidden="true" />返回顾客端
        </button>
        <p class="copyright">© 2026 茶序 CHAXU</p>
      </div>
    </aside>
    <div class="main-shell">
      <header class="topbar">
        <div class="store-title">
          <MapPin :size="18" aria-hidden="true" /><span>茶序 · 城市花园店</span
          ><span class="open-label">{{ staffArea ? "店员端" : "营业中" }}</span>
        </div>
        <div class="topbar-right">
          <span class="demo-label">{{
            staffArea ? "门店专属入口" : "本地体验版"
          }}</span>
          <template v-if="staffArea"
            ><LogoutButton v-if="auth.user" /><button
              v-if="canManageStore"
              class="text-button"
              @click="leaveStaff"
            >
              返回顾客端</button
            ><ShieldCheck v-else :size="21" aria-hidden="true"
          /></template>
          <RouterLink v-if="!auth.user && !staffArea" to="/login" class="inline-link">登录 / 注册</RouterLink>
          <RouterLink
            v-else-if="!staffArea"
            to="/me"
            class="user-avatar"
            aria-label="我的个人中心"
            >{{ auth.user?.displayName.slice(0, 1) || '茶' }}</RouterLink
          >
        </div>
      </header>
      <main id="main-content" tabindex="-1"><RouterView /></main>
      <footer class="page-footer">
        <span>{{
          staffArea ? "茶序 · 门店运营空间" : "认真选茶，用心做每一杯。"
        }}</span
        ><span><Leaf :size="13" aria-hidden="true" /> 原叶现萃 · 牧场鲜奶</span>
      </footer>
    </div>
    <nav
      class="mobile-nav"
      :aria-label="staffArea ? '门店移动导航' : '移动端导航'"
    >
      <RouterLink
        v-for="link in links"
        :key="link.to"
        :to="link.to"
        :class="{ 'nav-selected': active(link.to) }"
        :aria-current="active(link.to) ? 'page' : undefined"
        ><component :is="link.icon" :size="21" aria-hidden="true" />{{
          link.label
        }}</RouterLink
      >
    </nav>
    <div class="toast" role="status" aria-live="polite" v-show="shop.notice">
      {{ shop.notice }}
    </div>
    <p v-if="shop.storageWarning" class="storage-warning" role="alert">
      {{ shop.storageWarning }}
    </p>
  </div>
</template>
