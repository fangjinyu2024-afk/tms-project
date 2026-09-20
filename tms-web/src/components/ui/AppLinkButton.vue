<template>
  <button type="button" class="link-btn" :class="{ danger }" :disabled="disabled">
    <slot />
  </button>
</template>

<script setup lang="ts">
defineProps<{ danger?: boolean; disabled?: boolean }>()
</script>
