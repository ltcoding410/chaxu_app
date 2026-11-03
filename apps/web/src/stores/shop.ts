import { defineStore } from "pinia";
import { computed, ref, watch } from "vue";
import { products, templates } from "../data/catalog";
import { api, useAuth } from "./auth";
export interface CartLine {
  key: string;
  productId: number;
  name: string;
  spec: string;
  price: number;
  quantity: number;
}
export interface Coupon {
  id: string;
  templateId: number;
  expiresAt: number;
  used: boolean;
}
export interface Order {
  id: string;
  pickup: string;
  time: number;
  lines: CartLine[];
  total: number;
  paid: number;
  discount: number;
  earned: number;
  status: "待接单" | "制作中" | "待取餐" | "已完成";
}
interface Ledger {
  id: string;
  label: string;
  amount: number;
  time: number;
}
interface State {
  points: number;
  cart: CartLine[];
  coupons: Coupon[];
  orders: Order[];
  ledger: Ledger[];
  unavailable: number[];
}
const initial = (): State => ({
  points: 120,
  cart: [],
  coupons: [],
  orders: [],
  ledger: [{ id: "welcome", label: "体验积分", amount: 120, time: Date.now() }],
  unavailable: [],
});
function restore(key: string): State {
  try {
    const value = JSON.parse(localStorage.getItem(key) || "null");
    if (
      value &&
      Number.isFinite(value.points) &&
      ["cart", "coupons", "orders", "ledger", "unavailable"].every((k) =>
        Array.isArray(value[k]),
      )
    )
      return value;
  } catch {
    /* 存储不可用时使用内存演示 */
  }
  return initial();
}
export const useShop = defineStore("shop", () => {
  const auth = useAuth();
  const guest = (): State => ({ ...initial(), points: 0, ledger: [] });
  const state = ref<State>(guest());
  const storageKey = () => auth.user ? `chaxu-demo-user-${auth.user.id}` : "";
  watch(() => auth.user?.id, () => {
    state.value = auth.user ? restore(storageKey()) : guest();
  }, { flush: "sync", immediate: true });
  const notice = ref("");
  let timer: ReturnType<typeof setTimeout>;
  const storageWarning = ref("");
  watch(
    state,
    (value) => {
      try {
        if (auth.user) localStorage.setItem(storageKey(), JSON.stringify(value));
      } catch {
        storageWarning.value = "浏览器存储不可用，本次体验仅在当前页面保留。";
      }
    },
    { deep: true, flush: "sync" },
  );
  const total = computed(() =>
    state.value.cart.reduce((sum, line) => sum + line.price * line.quantity, 0),
  );
  const count = computed(() =>
    state.value.cart.reduce((sum, line) => sum + line.quantity, 0),
  );
  const availableCoupons = computed(() =>
    state.value.coupons.filter((c) => !c.used && c.expiresAt > Date.now()),
  );
  function tell(message: string) {
    notice.value = message;
    clearTimeout(timer);
    timer = setTimeout(() => {
      notice.value = "";
    }, 4000);
  }
  async function allowed() {
    try { return await auth.shoppingAccess(); }
    catch (error) { tell(error instanceof Error ? error.message : "暂时无法验证登录状态，请重试。"); return false; }
  }
  async function add(line: Omit<CartLine, "quantity">) {
    if (!await allowed()) return false;
    if (state.value.unavailable.includes(line.productId))
      return tell("这款饮品暂时售罄，请选择其他饮品。");
    const found = state.value.cart.find((x) => x.key === line.key);
    if (found) found.quantity += 1;
    else state.value.cart.push({ ...line, quantity: 1 });
    tell(`已加入 ${line.name}`);
  }
  async function quantity(key: string, delta: number) {
    if (!await allowed()) return;
    const line = state.value.cart.find((x) => x.key === key);
    if (!line) return;
    line.quantity += delta;
    state.value.cart = state.value.cart.filter((x) => x.quantity > 0);
  }
  async function redeem(id: number) {
    if (!await allowed()) return;
    const t = templates.find((x) => x.id === id);
    if (!t || state.value.points < t.cost)
      return tell("积分不足，再喝一杯好茶积攒积分吧。");
    if (state.value.coupons.filter((c) => c.templateId === id).length >= 3)
      return tell("每种体验券最多兑换 3 张。");
    state.value.points -= t.cost;
    state.value.coupons.push({
      id: crypto.randomUUID(),
      templateId: id,
      expiresAt: Date.now() + 30 * 86400000,
      used: false,
    });
    state.value.ledger.unshift({
      id: crypto.randomUUID(),
      label: `兑换「${t.name}」`,
      amount: -t.cost,
      time: Date.now(),
    });
    tell("兑换成功，优惠券已放入你的券包。");
  }
  async function checkout(couponId: string) {
    if (!await allowed()) return false;
    if (!count.value) return false;
    if (
      state.value.cart.some(
        (l) =>
          state.value.unavailable.includes(l.productId) ||
          !products.some((p) => p.id === l.productId),
      )
    ) {
      tell("购物袋中有售罄饮品，请先移除。");
      return false;
    }
    const coupon = availableCoupons.value.find((c) => c.id === couponId);
    const template = templates.find((t) => t.id === coupon?.templateId);
    if (couponId && (!coupon || !template || total.value < template.minimum)) {
      tell("这张券暂不满足使用条件，请重新选择。");
      return false;
    }
    const discount = template?.discount || 0;
    const paid = Math.max(0, total.value - discount);
    const earned = Math.floor(paid / 100);
    const order: Order = {
      id: crypto.randomUUID(),
      pickup: `A${String(state.value.orders.length + 1).padStart(3, "0")}`,
      time: Date.now(),
      lines: structuredClone(state.value.cart.map((x) => ({ ...x }))),
      total: total.value,
      paid,
      discount,
      earned,
      status: "待接单",
    };
    state.value.orders.unshift(order);
    if (coupon) coupon.used = true;
    state.value.points += earned;
    state.value.ledger.unshift({
      id: crypto.randomUUID(),
      label: `订单 ${order.pickup} 消费积分`,
      amount: earned,
      time: Date.now(),
    });
    state.value.cart = [];
    tell(`模拟下单成功，获得 ${earned} 积分。`);
    return true;
  }
  async function staffAllowed() {
    if (!auth.isStaff) { tell("仅授权店员可以管理门店。"); return false; }
    if (auth.isLoggingOut) return false;
    const expected = auth.user?.id;
    try { await api("/api/admin/access", {}); return !auth.isLoggingOut && auth.isStaff && auth.user?.id === expected; }
    catch (error) { tell(error instanceof Error ? error.message : "无法验证店员权限。"); return false; }
  }
  async function advance(id: string) {
    if (!auth.isStaff) return tell("仅授权店员可以处理订单。");
    if (!await staffAllowed()) return;
    const order = state.value.orders.find((o) => o.id === id);
    const steps: Order["status"][] = ["待接单", "制作中", "待取餐", "已完成"];
    if (order && order.status !== "已完成")
      order.status = steps[steps.indexOf(order.status) + 1]!;
  }
  async function toggleProduct(id: number) {
    if (!auth.isStaff) return tell("仅授权店员可以管理商品。");
    if (!await staffAllowed()) return;
    state.value.unavailable = state.value.unavailable.includes(id)
      ? state.value.unavailable.filter((x) => x !== id)
      : [...state.value.unavailable, id];
  }
  return {
    state,
    notice,
    storageWarning,
    total,
    count,
    availableCoupons,
    tell,
    add,
    quantity,
    redeem,
    checkout,
    advance,
    toggleProduct,
  };
});
