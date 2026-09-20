<template>
  <div class="panel" :class="{ 'panel-pad': pad }">
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{ pad?: boolean }>()
</script>
