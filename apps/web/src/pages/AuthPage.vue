<script setup lang="ts">
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Leaf, ArrowRight } from "lucide-vue-next";
import { useAuth } from "../stores/auth";
const auth = useAuth(), route = useRoute(), router = useRouter();
const registering = computed(() => route.query.mode === "register");
const username = ref(""), password = ref(""), displayName = ref(""), confirm = ref("");
const busy = ref(false), error = ref(""), success = ref("");
function switchMode() {
  error.value = ""; success.value = ""; password.value = ""; confirm.value = "";
  router.replace({ path: "/login", query: { ...route.query, mode: registering.value ? undefined : "register" } });
}
async function submit() {
  if (busy.value) return;
  error.value = ""; success.value = "";
  if (registering.value && password.value !== confirm.value) { error.value = "两次输入的密码不一致。"; return; }
  busy.value = true;
  try {
    if (registering.value) {
      await auth.register(username.value.trim(), password.value, displayName.value.trim());
      success.value = "注册成功，请使用新账号登录。";
      password.value = ""; confirm.value = "";
      await router.replace({ path: "/login", query: { ...route.query, mode: undefined } });
    } else {
      await auth.login(username.value.trim(), password.value);
      password.value = "";
      const destination = typeof route.query.redirect === "string" ? route.query.redirect : "/me";
      await router.replace(destination.startsWith("/") && !destination.startsWith("//") && !destination.startsWith("/login") ? destination : "/me");
    }
  } catch (cause) { error.value = cause instanceof Error ? cause.message : "操作失败，请重试。"; }
  finally { busy.value = false; }
}
</script>
<template>
  <div class="standard-page auth-page">
    <section class="auth-panel" aria-labelledby="auth-title">
      <Leaf :size="30" aria-hidden="true" />
      <h1 id="auth-title">{{ registering ? "加入茶序" : "欢迎回到茶序" }}</h1>
      <p>随时逛逛，喜欢再下单。登录后开启你的好茶日常。</p>
      <form class="auth-form" @submit.prevent="submit">
        <label for="auth-username">账号</label>
        <input id="auth-username" v-model="username" autocomplete="username" required minlength="4" maxlength="32" pattern="[a-zA-Z0-9_]{4,32}" placeholder="4–32 位字母、数字或下划线" :disabled="busy" />
        <template v-if="registering">
          <label for="auth-name">昵称</label>
          <input id="auth-name" v-model="displayName" autocomplete="nickname" required maxlength="50" placeholder="我们该怎么称呼你？" :disabled="busy" />
        </template>
        <label for="auth-password">密码</label>
        <input id="auth-password" v-model="password" type="password" :autocomplete="registering ? 'new-password' : 'current-password'" required :minlength="registering ? 8 : 1" maxlength="64" :pattern="registering ? '[!-~]{8,64}' : undefined" :disabled="busy" aria-describedby="password-help" />
        <small id="password-help">{{ registering ? "8–64 位英文字母、数字或符号，不含空格。" : "请输入注册时设置的密码。" }}</small>
        <template v-if="registering">
          <label for="auth-confirm">确认密码</label>
          <input id="auth-confirm" v-model="confirm" type="password" autocomplete="new-password" required maxlength="64" :disabled="busy" />
        </template>
        <p v-if="error" class="auth-error" role="alert">{{ error }}</p>
        <p v-if="success" class="auth-success" role="status">{{ success }}</p>
        <button class="button primary" type="submit" :disabled="busy">{{ busy ? "正在处理…" : registering ? "创建账号" : "登录" }}<ArrowRight :size="17" aria-hidden="true" /></button>
      </form>
      <button class="text-button auth-switch" :disabled="busy" @click="switchMode">{{ registering ? "已有账号？去登录" : "还没有账号？立即注册" }}</button>
      <RouterLink to="/" class="inline-link">先逛逛饮品，不登录也能浏览</RouterLink>
    </section>
  </div>
</template>
