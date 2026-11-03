import { createApp } from "vue";
import { createPinia } from "pinia";
import { createRouter, createWebHistory } from "vue-router";
import App from "./App.vue";
import MenuPage from "./pages/MenuPage.vue";
import MemberPage from "./pages/MemberPage.vue";
import OrdersPage from "./pages/OrdersPage.vue";
import AdminPage from "./pages/AdminPage.vue";
import MyPage from "./pages/MyPage.vue";
import StaffEntryPage from "./pages/StaffEntryPage.vue";
import AuthPage from "./pages/AuthPage.vue";
import { useAuth } from "./stores/auth";
import "./styles.css";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", component: MenuPage, meta: { title: "在线点单" } },
    { path: "/login", component: AuthPage, meta: { title: "登录与注册" } },
    { path: "/member", component: MemberPage, meta: { title: "会员中心" } },
    { path: "/me", component: MyPage, meta: { title: "我的" } },
    { path: "/orders", component: OrdersPage, meta: { title: "我的订单" } },
    {
      path: "/staff/login",
      component: StaffEntryPage,
      meta: { title: "店员入口", staffArea: true },
    },
    {
      path: "/admin",
      component: AdminPage,
      meta: {
        title: "门店工作台",
        staffArea: true,
      },
    },
    { path: "/:pathMatch(.*)*", redirect: "/" },
  ],
  scrollBehavior: (to) => (to.hash ? { el: to.hash, top: 24 } : { top: 0 }),
});
router.afterEach((to) => {
  document.title = `${String(to.meta.title)} · 茶序 CHAXU`;
});
const app = createApp(App);
const pinia = createPinia();
app.use(pinia).use(router);
// 页面公开可浏览；在首次展示个人状态前恢复服务端会话。
useAuth(pinia).refresh().finally(() => app.mount("#app"));
