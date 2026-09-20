<template>
  <div class="filterbar">
    <label class="filter-field">
      {{ t('common.keyword') }}
      <input
        :value="keyword"
        :placeholder="placeholder ?? t('common.inputPlaceholder')"
        @input="emit('update:keyword', ($event.target as HTMLInputElement).value)"
        @keydown.enter="emit('search')"
      />
    </label>
    <label v-for="field in fields" :key="field.field" class="filter-field">
      {{ t(field.labelKey) }}
      <select
        :value="values[field.field] ?? ''"
        @change="emit('update:value', field.field, ($event.target as HTMLSelectElement).value)"
      >
        <option value="">{{ t('common.all') }}</option>
        <option v-for="option in field.options" :key="option.value" :value="option.value">
          {{ t(option.labelKey) }}
        </option>
      </select>
    </label>
    <slot />
    <div class="actions">
      <AppButton variant="primary" @click="emit('search')">{{ t('common.search') }}</AppButton>
      <AppButton @click="emit('reset')">{{ t('common.reset') }}</AppButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import AppButton from './AppButton.vue'

/** 筛选项取值来自 stores/dict.ts 的枚举，按完整取值渲染，不按当前页数据推导（详细设计 6.1） */
export interface FilterField {
  field: string
  labelKey: string
  options: { value: string; labelKey: string }[]
}

defineProps<{
  fields: FilterField[]
  values: Record<string, string | undefined>
  keyword?: string
  placeholder?: string
}>()

const emit = defineEmits<{
  (event: 'update:keyword', value: string): void
  (event: 'update:value', field: string, value: string): void
  (event: 'search'): void
  (event: 'reset'): void
}>()

const { t } = useI18n()
</script>
