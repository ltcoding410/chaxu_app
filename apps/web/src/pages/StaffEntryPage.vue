<script setup lang="ts">
import { ShieldCheck } from "lucide-vue-next";
import { useAuth } from "../stores/auth";
const auth = useAuth();
</script>
<template>
  <div class="standard-page staff-entry-page">
    <section class="staff-entry" aria-labelledby="staff-entry-title">
      <ShieldCheck :size="34" aria-hidden="true" />
      <h1 id="staff-entry-title">门店工作空间</h1>
      <p class="staff-entry-intro">每一杯好茶，从有序的日常开始。</p>
      <p>可以浏览工作台布局。处理订单和管理商品仅向授权店员开放，注册顾客账号不会获得员工权限。</p>
      <RouterLink to="/admin" class="button secondary">{{ auth.isStaff ? '进入工作台' : '只读浏览工作台' }}</RouterLink>
      <RouterLink v-if="!auth.user" :to="{ path: '/login', query: { redirect: '/admin' } }" class="button primary">使用店员账号登录</RouterLink>
      <p v-else-if="!auth.isStaff">当前是顾客账号，如需员工权限，请联系门店管理员。</p>
      <RouterLink to="/" class="inline-link">返回顾客点单页</RouterLink>
    </section>
  </div>
</template>
