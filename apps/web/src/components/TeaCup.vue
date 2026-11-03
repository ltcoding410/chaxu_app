<script setup lang="ts">
defineProps<{ tone: string; tea: string; small?: boolean }>();
</script>
<template>
  <div
    class="drink-art"
    :class="[tone, { small }]"
    :style="{ '--tea': tea }"
    aria-hidden="true"
  >
    <span class="art-orbit"></span><span class="tea-leaf leaf-one"></span
    ><span class="tea-leaf leaf-two"></span>
    <div class="cup-shadow"></div>
    <div class="tea-cup">
      <div class="cup-lid"></div>
      <div class="cup-liquid"></div>
      <span class="cup-brand">茶 序<small>CHAXU</small></span
      ><span class="cup-foot">一杯好茶 · 一点日常</span>
    </div>
  </div>
</template>
