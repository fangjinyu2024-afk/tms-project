<template>
  <div class="page">
    <el-card>
      <div class="banner">
        <div>
          <h2>{{ greeting }}，{{ user.member?.nickname }}</h2>
          <p class="form-tip">
            当前身份：{{ user.member?.tenantName }} · {{ user.member?.orgName }}（{{
              user.isPlatform ? '平台账户' : '客户账户'
            }}）
          </p>
        </div>
        <el-tag type="info">已开通菜单 {{ user.menus.length }} 项</el-tag>
      </div>
    </el-card>

    <el-card class="section">
      <template #header>快捷入口</template>
      <el-space wrap>
        <el-button
          v-for="entry in entries"
          :key="entry.name"
          @click="router.push({ name: entry.name })"
        >
          {{ entry.title }}
        </el-button>
      </el-space>
      <el-empty v-if="!entries.length" description="当前账户尚未获得任何菜单权限" />
    </el-card>

    <el-alert
      class="section"
      type="info"
      :closable="false"
      title="工作台统计与图表随详细设计 3.20 章节实现"
      description="本轮交付基座模块（认证与会话、权限与角色、客户与功能授权、机构、成员、日志与会话、产品与型号）。"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const user = useUserStore()

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 12) return '上午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const entries = computed(() =>
  router
    .getRoutes()
    .filter((record) => {
      const permCode = record.meta.permCode as string | undefined
      return Boolean(record.meta.menuKey) && Boolean(permCode) && user.has(permCode as string)
    })
    .map((record) => ({ name: String(record.name), title: String(record.meta.title) }))
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
