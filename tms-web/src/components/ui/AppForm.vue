<template>
  <form class="form-grid" novalidate @submit.prevent="emit('submit')">
    <slot />
  </form>
</template>

<script setup lang="ts">
const emit = defineEmits<{ (event: 'submit'): void }>()
</script>
