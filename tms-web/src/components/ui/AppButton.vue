<template>
  <button type="button" class="btn" :class="variantClass" :disabled="disabled">
    <AppIcon v-if="icon" :name="icon" />
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AppIcon, { type IconName } from './AppIcon.vue'

const props = withDefaults(
  defineProps<{
    variant?: 'default' | 'primary' | 'small' | 'danger'
    icon?: IconName
    disabled?: boolean
  }>(),
  { variant: 'default' }
)

const variantClass = computed(() => (props.variant === 'default' ? '' : props.variant))
</script>
