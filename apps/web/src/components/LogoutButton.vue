<script setup lang="ts">
import { ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useAuth } from "../stores/auth";
import { useShop } from "../stores/shop";

const auth = useAuth(), shop = useShop(), router = useRouter();
const dialog = ref<HTMLDialogElement>();
const error = ref("");
function open() {
  error.value = "";
  dialog.value?.showModal();
}
watch(() => auth.user?.id, () => { dialog.value?.close(); });
async function confirmLogout() {
  if (auth.isLoggingOut) return;
  error.value = "";
  try {
    await auth.logout();
    dialog.value?.close();
    await router.replace("/");
    shop.tell("已安全退出登录，可继续浏览饮品。");
  } catch {
    error.value = "暂时无法确认是否退出成功，请检查网络后重试。为确保账号安全，公共设备上请勿直接离开。";
  }
}
</script>

<template>
  <button class="text-button" :disabled="auth.isLoggingOut" @click="open">{{ auth.isLoggingOut ? "正在退出…" : "退出登录" }}</button>
  <Teleport to="body">
    <dialog ref="dialog" class="logout-dialog" aria-labelledby="logout-title" aria-describedby="logout-description" :aria-busy="auth.isLoggingOut" @cancel="auth.isLoggingOut && $event.preventDefault()">
      <h2 id="logout-title">退出当前账号？</h2>
      <p id="logout-description">退出后将隐藏当前账号的订单、积分和优惠券，同一浏览器的其他页面也会同步登录状态。</p>
      <p v-if="shop.count">购物袋中的 {{ shop.count }} 件饮品会在本机保留，下次登录此账号可继续选购。</p>
      <p v-if="shop.state.orders.some(order => order.status !== '已完成')">你还有未完成的体验订单。退出不会取消订单，再次登录此账号可查看取餐信息。</p>
      <p v-if="error" class="auth-error" role="alert">{{ error }}</p>
      <div class="logout-actions">
        <button class="button secondary" autofocus :disabled="auth.isLoggingOut" @click="dialog?.close()">暂不退出</button>
        <button class="button primary" :disabled="auth.isLoggingOut" @click="confirmLogout">{{ auth.isLoggingOut ? "正在退出…" : error ? "重试退出" : "确认退出" }}</button>
      </div>
    </dialog>
  </Teleport>
</template>
