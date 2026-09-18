<template>
  <el-container class="layout">
    <el-aside width="226px" class="sidebar">
      <div class="brand">
        <div class="brand-icon">T</div>
        <div>
          <strong>TMS</strong>
          <small>终端管理系统</small>
        </div>
      </div>
      <el-menu :default-active="route.name as string" router class="menu">
        <template v-for="group in menuGroups" :key="group.name">
          <template v-if="group.name === '工作台'">
            <el-menu-item
              v-for="item in group.items"
              :key="item.name"
              :index="item.name"
              :route="{ name: item.name }"
            >
              {{ item.title }}
            </el-menu-item>
          </template>
          <el-sub-menu v-else :index="group.name">
            <template #title>{{ group.name }}</template>
            <el-menu-item
              v-for="item in group.items"
              :key="item.name"
              :index="item.name"
              :route="{ name: item.name }"
            >
              {{ item.title }}
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="crumb">
          <span class="org">{{ orgLabel }}</span>
          <span class="title">{{ route.meta.title }}</span>
        </div>
        <el-dropdown @command="onCommand">
          <span class="account">
            {{ user.member?.nickname }}（{{ user.member?.account }}）
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

interface MenuItem {
  name: string
  title: string
}

/** 平台租户与其根机构同名时只展示一次 */
const orgLabel = computed(() => {
  const tenantName = user.member?.tenantName
  const orgName = user.member?.orgName
  return tenantName === orgName ? orgName : `${tenantName} · ${orgName}`
})

/** 侧边菜单按有效权限渲染：菜单键已开通且持有该菜单的查看权限 */
const menuGroups = computed(() => {
  const groups = new Map<string, MenuItem[]>()
  for (const record of router.getRoutes()) {
    const menuKey = record.meta.menuKey as string | undefined
    const group = record.meta.group as string | undefined
    if (!menuKey || !group || !record.name) {
      continue
    }
    const permCode = record.meta.permCode as string | undefined
    const visible = menuKey === 'home' ? user.hasMenu('home') : permCode ? user.has(permCode) : false
    if (!visible) {
      continue
    }
    const items = groups.get(group) ?? []
    items.push({ name: String(record.name), title: String(record.meta.title) })
    groups.set(group, items)
  }
  const order = ['工作台', '设备管理', '远程维护', '激活与证书', '系统管理']
  return Array.from(groups, ([name, items]) => ({ name, items })).sort(
    (a, b) => order.indexOf(a.name) - order.indexOf(b.name)
  )
})

async function onCommand(command: string) {
  if (command === 'logout') {
    await user.logout()
    await router.push({ name: 'login' })
    return
  }
  await router.push({ name: command === 'profile' ? 'profile' : 'change-password' })
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.sidebar {
  background: #fff;
  border-right: 1px solid #e7ebf2;
}

.brand {
  display: flex;
  align-items: center;
  gap: 11px;
  height: 64px;
  padding: 0 20px;
  border-bottom: 1px solid #e7ebf2;
}

.brand-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: #1677ff;
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 700;
}

.brand small {
  display: block;
  color: #66778d;
  font-size: 11px;
}

.menu {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e7ebf2;
}

.crumb .org {
  color: #66778d;
  margin-right: 10px;
  font-size: 13px;
}

.crumb .title {
  font-size: 16px;
  font-weight: 600;
}

.account {
  cursor: pointer;
  color: #1f2c42;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.main {
  padding: 0;
  overflow: auto;
}
</style>
