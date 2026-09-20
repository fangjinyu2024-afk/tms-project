<template>
  <div class="segmented">
    <button
      v-for="option in options"
      :key="option.value"
      type="button"
      :class="{ active: option.value === modelValue }"
      @click="emit('update:modelValue', option.value)"
    >
      {{ t(option.labelKey) }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

defineProps<{ modelValue: string; options: { value: string; labelKey: string }[] }>()
const emit = defineEmits<{ (event: 'update:modelValue', value: string): void }>()

const { t } = useI18n()
</script>
