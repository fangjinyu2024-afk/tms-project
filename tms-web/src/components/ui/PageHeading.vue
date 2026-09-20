<template>
  <div class="heading">
    <div>
      <h1>{{ title }}</h1>
      <p v-if="description" class="heading-sub">{{ description }}</p>
    </div>
    <div class="actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string; description?: string }>()
</script>
