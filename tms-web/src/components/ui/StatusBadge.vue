<template>
  <span class="badge" :class="tone">{{ label }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

/**
 * 状态徽标。原型按中文文案正则判定颜色，国际化后会失配，这里改为按枚举编码映射
 * （规范差异登记表第 3 条）。取值以详细设计第 6 章为唯一来源。
 */
const GREEN = ['ENABLED', 'SUCCESS', 'ACTIVATED', 'NORMAL', 'AVAILABLE', 'VALID', 'COMPLETED']
const RED = ['DISABLED', 'FAIL', 'STOPPED', 'SCRAPPED', 'EXPIRED', 'REVOKED', 'TRIGGERED']
const BLUE = ['RUNNING', 'PUBLISHED', 'PRODUCT', 'SHIPPED']
const ORANGE = ['PENDING', 'PARTIAL', 'INACTIVE', 'EXPIRING', 'LOCKED', 'DRAFT']

const props = defineProps<{ code: string; labelKey?: string }>()

const { t } = useI18n()

const label = computed(() => (props.labelKey ? t(props.labelKey) : props.code))

const tone = computed(() => {
  if (GREEN.includes(props.code)) return 'green'
  if (RED.includes(props.code)) return 'red'
  if (BLUE.includes(props.code)) return 'blue'
  if (ORANGE.includes(props.code)) return 'orange'
  return ''
})
</script>
