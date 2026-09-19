<template>
  <div class="page">
    <el-card>
      <div class="banner">
        <div>
          <h2>{{ t('home.greetingLine', { greeting, name: user.member?.nickname }) }}</h2>
          <p class="form-tip">{{ identity }}</p>
        </div>
        <el-tag type="info">{{ t('home.openedMenus', { count: user.menus.length }) }}</el-tag>
      </div>
    </el-card>

    <el-card class="section">
      <template #header>{{ t('home.quickEntries') }}</template>
      <el-space wrap>
        <el-button
          v-for="entry in entries"
          :key="entry.name"
          @click="router.push({ name: entry.name })"
        >
          {{ t(entry.titleKey) }}
        </el-button>
      </el-space>
      <el-empty v-if="!entries.length" :description="t('home.noPermission')" />
    </el-card>

    <el-alert
      class="section"
      type="info"
      :closable="false"
      :title="t('home.pendingTitle')"
      :description="t('home.pendingDescription')"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const user = useUserStore()
const { t } = useI18n()

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return t('home.greetingNight')
  if (hour < 12) return t('home.greetingMorning')
  if (hour < 18) return t('home.greetingAfternoon')
  return t('home.greetingEvening')
})

const identity = computed(() =>
  t('common.labelValue', {
    label: t('home.identity'),
    value: t('common.parenthesized', {
      main: `${user.member?.tenantName} · ${user.member?.orgName}`,
      sub: user.isPlatform ? t('layout.platformAccount') : t('layout.tenantAccount')
    })
  })
)

const entries = computed(() =>
  router
    .getRoutes()
    .filter((record) => {
      const permCode = record.meta.permCode as string | undefined
      return Boolean(record.meta.menuKey) && Boolean(permCode) && user.has(permCode as string)
    })
    .map((record) => ({ name: String(record.name), titleKey: String(record.meta.titleKey) }))
)
</script>

<style scoped>
.banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.banner h2 {
  margin: 0 0 6px;
}

.section {
  margin-top: 16px;
}
</style>
