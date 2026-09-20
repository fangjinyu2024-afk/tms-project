<template>
  <div class="empty">
    <AppIcon name="box" />
    <div>{{ text ?? t('common.noMatchedData') }}</div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import AppIcon from './AppIcon.vue'

defineProps<{ text?: string }>()

const { t } = useI18n()
</script>
