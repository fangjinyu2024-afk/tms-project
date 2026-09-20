<template>
  <div class="notice" :class="{ warn }"><slot /></div>
</template>

<script setup lang="ts">
defineProps<{ warn?: boolean }>()
</script>
