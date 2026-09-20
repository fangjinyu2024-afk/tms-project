<template>
  <AppDialog :model-value="modelValue" :title="title" drawer @update:model-value="emit('update:modelValue', $event)">
    <slot />
    <template v-if="$slots.footer" #footer><slot name="footer" /></template>
  </AppDialog>
</template>

<script setup lang="ts">
import AppDialog from './AppDialog.vue'

defineProps<{ modelValue: boolean; title: string }>()
const emit = defineEmits<{ (event: 'update:modelValue', value: boolean): void }>()
</script>
