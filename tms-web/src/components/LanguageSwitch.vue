<template>
  <el-dropdown trigger="click" @command="onSelect">
    <span class="switch">
      {{ t('common.labelValue', { label: t('language.label'), value: t(activeLabelKey) }) }}
      <el-icon><ArrowDown /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="option in LOCALE_OPTIONS"
          :key="option.value"
          :command="option.value"
          :disabled="option.value === locale"
        >
          {{ t(option.labelKey) }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowDown } from '@element-plus/icons-vue'
import { LOCALE_OPTIONS, setLocale, type AppLocale } from '@/i18n'

const { t, locale } = useI18n()

const activeLabelKey = computed(
  () => LOCALE_OPTIONS.find((option) => option.value === locale.value)?.labelKey ?? 'language.zh'
)

function onSelect(value: AppLocale) {
  setLocale(value)
}
</script>

<style scoped>
.switch {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: inherit;
  font-size: 13px;
}
</style>
