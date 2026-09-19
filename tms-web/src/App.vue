<template>
  <el-config-provider :locale="elementLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ELEMENT_LOCALES, type AppLocale } from '@/i18n'

const { locale } = useI18n()
const elementLocale = computed(() => ELEMENT_LOCALES[locale.value as AppLocale])
</script>
