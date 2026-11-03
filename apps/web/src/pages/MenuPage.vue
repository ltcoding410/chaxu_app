<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  ArrowRight,
  ArrowUpRight as ArrowUpRightIcon,
  Search,
  Plus,
  Minus,
  ShoppingBag,
  X,
  Clock3,
  Leaf,
  Ticket,
  ChevronRight,
} from "lucide-vue-next";
import {
  categories,
  money,
  products,
  templates,
  type Product,
} from "../data/catalog";
import TeaCup from "../components/TeaCup.vue";
import { useShop } from "../stores/shop";
const route = useRoute(),
  router = useRouter(),
  shop = useShop();
const category = computed(() =>
  typeof route.query.category === "string" &&
  categories.includes(route.query.category)
    ? route.query.category
    : "全部饮品",
);
const search = ref(typeof route.query.q === "string" ? route.query.q : "");
watch(search, (q) =>
  router.replace({ query: { ...route.query, q: q || undefined } }),
);
watch(
  () => route.query.q,
  (q) => {
    search.value = typeof q === "string" ? q : "";
  },
);
const filtered = computed(() =>
  products.filter(
    (p) =>
      (category.value === "全部饮品" ||
        (category.value === "人气推荐"
          ? !!p.badge
          : p.category === category.value)) &&
      `${p.name}${p.description}`.includes(search.value.trim()),
  ),
);
const selected = ref<Product | null>(null),
  size = ref("标准杯"),
  temperature = ref("正常冰"),
  sugar = ref("五分糖"),
  topping = ref("不加料");
const modal = ref<HTMLDialogElement>(),
  checkoutModal = ref<HTMLDialogElement>();
const couponTemplate = (id: number) => templates.find((t) => t.id === id)!;
const couponId = ref(""),
  busy = ref(false);
const unitPrice = computed(
  () =>
    (selected.value?.price || 0) +
    (size.value === "加大杯" ? 300 : 0) +
    (topping.value === "珍珠" ? 200 : 0),
);
const discount = computed(() => {
  const c = shop.availableCoupons.find((c) => c.id === couponId.value);
  return templates.find((t) => t.id === c?.templateId)?.discount || 0;
});
async function configure(product: Product) {
  selected.value = product;
  size.value = "标准杯";
  temperature.value = "正常冰";
  sugar.value = "五分糖";
  topping.value = "不加料";
  await nextTick();
  modal.value?.showModal();
}
async function add() {
  if (!selected.value) return;
  const spec = [size.value, temperature.value, sugar.value, topping.value].join(
    " / ",
  );
  await shop.add({
    key: `${selected.value.id}-${spec}`,
    productId: selected.value.id,
    name: selected.value.name,
    spec,
    price: unitPrice.value,
  });
  modal.value?.close();
}
function openCheckout() {
  couponId.value = "";
  checkoutModal.value?.showModal();
}
async function pay() {
  if (busy.value) return;
  busy.value = true;
  try {
    if (await shop.checkout(couponId.value)) {
      checkoutModal.value?.close();
      await router.push("/orders");
    }
  } finally {
    busy.value = false;
  }
}
const choices = computed(() => [
  { label: "杯型", model: size, options: ["标准杯", "加大杯"] },
  {
    label: "温度",
    model: temperature,
    options: ["正常冰", "少冰", "去冰", "热饮"],
  },
  {
    label: "甜度",
    model: sugar,
    options: ["标准糖", "五分糖", "三分糖", "不另加糖"],
  },
  { label: "加料", model: topping, options: ["不加料", "珍珠"] },
]);
</script>
<template>
  <div class="menu-layout">
    <div class="menu-main">
      <section class="hero">
        <div class="hero-copy">
          <span class="season-label"
            ><Leaf :size="14" aria-hidden="true" /> 秋日茶事 · 新鲜上线</span
          >
          <h1>把日常，<br />泡成喜欢的样子。</h1>
          <p>一缕原叶茶香，一口鲜醇好奶。<br />今天的好心情，从这一杯开始。</p>
          <button class="hero-action" @click="configure(products[1]!)">
            尝一口山野栀子青<ArrowRight :size="17" aria-hidden="true" /></button
          ><span class="hero-bottom">好原叶 / 好鲜奶 / 好一杯</span>
        </div>
        <div class="hero-art">
          <span class="hero-round"></span
          ><span class="hero-calligraphy">茶有序<br />心自在</span
          ><TeaCup tone="gardenia" tea="#c7cd9e" /><span class="hero-stamp"
            >原叶<br />现萃</span
          >
        </div>
      </section>
      <div class="benefits">
        <span><Leaf :size="16" aria-hidden="true" /> 真茶真奶，现点现做</span
        ><span><Clock3 :size="16" aria-hidden="true" /> 到店自取，无需等待</span
        ><RouterLink to="/member"
          ><Ticket :size="16" aria-hidden="true" />
          每喝一杯，都有回馈<ChevronRight :size="14" aria-hidden="true"
        /></RouterLink>
      </div>
      <section aria-labelledby="menu-heading" class="menu-section">
        <div class="section-heading">
          <div>
            <p class="eyebrow">为你现萃</p>
            <h2 id="menu-heading">今天，想喝点什么？</h2>
          </div>
          <label class="search-box"
            ><Search :size="17" aria-hidden="true" /><span class="sr-only"
              >搜索饮品</span
            ><input
              v-model="search"
              name="search"
              autocomplete="off"
              placeholder="找一杯喜欢的茶…"
          /></label>
        </div>
        <nav class="category-tabs" aria-label="饮品分类">
          <RouterLink
            v-for="item in categories"
            :key="item"
            :to="{
              path: '/',
              query: {
                ...route.query,
                category: item === '全部饮品' ? undefined : item,
              },
            }"
            :class="{ selected: category === item }"
            :aria-current="category === item ? 'true' : undefined"
            >{{ item }}</RouterLink
          >
        </nav>
        <div class="product-grid" v-if="filtered.length">
          <article
            class="product-card"
            v-for="product in filtered"
            :key="product.id"
          >
            <div class="product-image">
              <TeaCup :tone="product.tone" :tea="product.tea" /><span
                class="product-badge"
                v-if="product.badge"
                >{{ product.badge }}</span
              ><span
                class="sold-out"
                v-if="shop.state.unavailable.includes(product.id)"
                >暂时售罄</span
              >
            </div>
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <p class="english-name">{{ product.english }}</p>
              <p class="product-description">{{ product.description }}</p>
              <div class="product-bottom">
                <strong>{{ money(product.price) }}<small>起</small></strong
                ><button
                  class="add-button"
                  :disabled="shop.state.unavailable.includes(product.id)"
                  :aria-label="`选择${product.name}规格`"
                  @click="configure(product)"
                >
                  <Plus :size="20" aria-hidden="true" />
                </button>
              </div>
            </div>
          </article>
        </div>
        <div v-else class="empty-state">
          <Search :size="30" aria-hidden="true" />
          <h3>还没找到这杯茶</h3>
          <p>换个关键词，或看看全部饮品。</p>
          <button
            class="button secondary"
            @click="
              search = '';
              router.replace('/');
            "
          >
            查看全部饮品
          </button>
        </div>
      </section>
    </div>
    <aside class="order-rail" aria-label="购物袋与会员权益">
      <section class="pickup-card">
        <span class="eyebrow">你的取茶方式</span>
        <div>
          <ShoppingBag :size="22" aria-hidden="true" />
          <h2>到店自取</h2>
          <span class="soft-tag">免配送费</span>
        </div>
        <p>城市花园店 · 约 10–15 分钟</p>
      </section>
      <section class="bag-card" id="shopping-bag">
        <div class="bag-heading">
          <h2>
            我的购物袋 <span>{{ shop.count }}</span>
          </h2>
          <button
            v-if="shop.count"
            class="text-button"
            @click="
              shop.state.cart = [];
              shop.tell('购物袋已清空');
            "
          >
            清空
          </button>
        </div>
        <div v-if="!shop.count" class="bag-empty">
          <ShoppingBag :size="42" :stroke-width="1" aria-hidden="true" />
          <p>购物袋还空着</p>
          <span>挑一杯喜欢的，犒劳一下自己。</span>
        </div>
        <div v-else class="bag-lines">
          <div class="bag-line" v-for="line in shop.state.cart" :key="line.key">
            <h3>{{ line.name }}</h3>
            <p>{{ line.spec }}</p>
            <div>
              <strong>{{ money(line.price * line.quantity) }}</strong>
              <div class="stepper">
                <button
                  :aria-label="`减少${line.name}`"
                  @click="shop.quantity(line.key, -1)"
                >
                  <Minus :size="13" aria-hidden="true" /></button
                ><span>{{ line.quantity }}</span
                ><button
                  :aria-label="`增加${line.name}`"
                  @click="shop.quantity(line.key, 1)"
                >
                  <Plus :size="13" aria-hidden="true" />
                </button>
              </div>
            </div>
          </div>
        </div>
        <div class="bag-summary">
          <p>
            <span>商品合计</span><strong>{{ money(shop.total) }}</strong>
          </p>
          <span class="points-hint">实付每满 1 元，可得 1 积分</span
          ><button
            class="button primary checkout-button"
            :disabled="!shop.count"
            @click="openCheckout"
          >
            去结算<span
              >{{ money(shop.total) }}<ArrowRight :size="16" aria-hidden="true"
            /></span>
          </button>
        </div>
      </section>
      <RouterLink to="/member" class="member-promo"
        ><span class="promo-symbol">茶</span
        ><span class="eyebrow">好茶常伴，好礼相随</span>
        <h3>每一份喜欢<br />都有积分回馈。</h3>
        <p>100 积分，兑换 5 元优惠券</p>
        <span class="promo-link">探索会员权益<ArrowUpRightIcon /></span
      ></RouterLink>
      <p class="rail-note">
        本地体验 · 无真实支付<br />积分与订单仅保存在当前浏览器
      </p>
    </aside>
  </div>
  <a v-if="shop.count" href="#shopping-bag" class="mobile-bag-shortcut">
    <ShoppingBag :size="19" aria-hidden="true" />
    <span>{{ shop.count }} 杯 · {{ money(shop.total) }}</span>
    <strong>查看购物袋</strong><ChevronRight :size="16" aria-hidden="true" />
  </a>
  <dialog ref="modal" class="spec-dialog" aria-labelledby="spec-title">
    <form @submit.prevent="add" v-if="selected">
      <div class="dialog-top">
        <span class="eyebrow">定制你的这一杯</span
        ><button
          type="button"
          class="icon-button"
          aria-label="关闭规格选择"
          @click="modal?.close()"
        >
          <X :size="21" aria-hidden="true" />
        </button>
      </div>
      <h2 id="spec-title">{{ selected.name }}</h2>
      <p class="muted">{{ selected.description }}</p>
      <fieldset v-for="group in choices" :key="group.label">
        <legend>
          {{ group.label
          }}<span v-if="group.label === '杯型'">加大杯 +3 元</span
          ><span v-if="group.label === '加料'">珍珠 +2 元</span>
        </legend>
        <div class="option-row">
          <label
            v-for="option in group.options"
            :key="option"
            class="option-chip"
            ><input
              type="radio"
              :name="group.label"
              :value="option"
              v-model="group.model.value"
            /><span>{{ option }}</span></label
          >
        </div>
      </fieldset>
      <div class="dialog-footer">
        <strong>{{ money(unitPrice) }}</strong
        ><button class="button primary" type="submit">
          加入购物袋<Plus :size="17" aria-hidden="true" />
        </button>
      </div>
    </form>
  </dialog>
  <dialog
    ref="checkoutModal"
    class="spec-dialog"
    aria-labelledby="checkout-title"
  >
    <div class="dialog-top">
      <span class="eyebrow">确认你的好茶</span
      ><button
        class="icon-button"
        aria-label="关闭结算"
        @click="checkoutModal?.close()"
      >
        <X :size="21" aria-hidden="true" />
      </button>
    </div>
    <h2 id="checkout-title">到店自取 · 城市花园店</h2>
    <p class="muted">约 10–15 分钟，制作完成后到店取茶。</p>
    <div class="checkout-lines">
      <p v-for="line in shop.state.cart" :key="line.key">
        <span>{{ line.name }} × {{ line.quantity }}</span
        ><strong>{{ money(line.price * line.quantity) }}</strong>
      </p>
    </div>
    <label class="field-label" for="coupon">使用优惠券</label
    ><select id="coupon" v-model="couponId">
      <option value="">不使用优惠券</option>
      <option
        v-for="c in shop.availableCoupons"
        :key="c.id"
        :value="c.id"
        :disabled="shop.total < couponTemplate(c.templateId).minimum"
      >
        {{ couponTemplate(c.templateId).name }} ·
        {{ couponTemplate(c.templateId).description }}减
        {{ money(couponTemplate(c.templateId).discount) }}
      </option>
    </select>
    <p class="checkout-discount">
      优惠抵扣 <strong>−{{ money(discount) }}</strong>
    </p>
    <p class="demo-explanation">
      此为模拟结算，不会产生真实扣款。完成后可在“我的订单”查看取餐码和制作进度。
    </p>
    <div class="dialog-footer">
      <strong>{{ money(shop.total - discount) }}</strong
      ><button class="button primary" :disabled="busy" @click="pay">
        {{ busy ? "提交中…" : "确认模拟下单"
        }}<ArrowRight :size="17" aria-hidden="true" />
      </button>
    </div>
  </dialog>
</template>
