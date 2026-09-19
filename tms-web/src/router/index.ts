import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/layouts/MainLayout.vue'

/**
 * 路由元数据绑定菜单键、分组键与权限码，菜单标题走 i18n，按有效权限渲染；
 * 业务模块（设备、远程维护、激活与证书）随对应章节的详细设计补充。
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, titleKey: 'menu.login' }
  },
  {
    path: '/change-password',
    name: 'change-password',
    component: () => import('@/views/ChangePasswordView.vue'),
    meta: { titleKey: 'menu.changePassword' }
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/views/HomeView.vue'),
        meta: { titleKey: 'menu.home', menuKey: 'home', group: 'home' }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/ProfileView.vue'),
        meta: { titleKey: 'menu.profile' }
      },
      {
        path: 'system/customers',
        name: 'customers',
        component: () => import('@/views/system/CustomerListView.vue'),
        meta: { titleKey: 'menu.customers', menuKey: 'customers', group: 'system', permCode: 'customers:view' }
      },
      {
        path: 'system/orgs',
        name: 'orgs',
        component: () => import('@/views/system/OrgListView.vue'),
        meta: { titleKey: 'menu.orgs', menuKey: 'orgs', group: 'system', permCode: 'orgs:view' }
      },
      {
        path: 'system/members',
        name: 'members',
        component: () => import('@/views/system/MemberListView.vue'),
        meta: { titleKey: 'menu.members', menuKey: 'members', group: 'system', permCode: 'members:view' }
      },
      {
        path: 'system/roles',
        name: 'roles',
        component: () => import('@/views/system/RoleListView.vue'),
        meta: { titleKey: 'menu.roles', menuKey: 'roles', group: 'system', permCode: 'roles:view' }
      },
      {
        path: 'system/products',
        name: 'products',
        component: () => import('@/views/system/ProductListView.vue'),
        meta: { titleKey: 'menu.products', menuKey: 'products', group: 'system', permCode: 'products:view' }
      },
      {
        path: 'system/oper-logs',
        name: 'logs',
        component: () => import('@/views/system/OperLogView.vue'),
        meta: { titleKey: 'menu.logs', menuKey: 'logs', group: 'system', permCode: 'logs:view' }
      },
      {
        path: 'system/login-logs',
        name: 'logins',
        component: () => import('@/views/system/LoginLogView.vue'),
        meta: { titleKey: 'menu.logins', menuKey: 'logins', group: 'system', permCode: 'logins:view' }
      },
      {
        path: 'system/sessions',
        name: 'sessions',
        component: () => import('@/views/system/SessionListView.vue'),
        meta: { titleKey: 'menu.sessions', menuKey: 'sessions', group: 'system', permCode: 'sessions:view' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const store = useUserStore()
  if (to.meta.public) {
    return true
  }
  if (!store.token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (!store.loaded) {
    try {
      await store.loadProfile()
    } catch {
      return { name: 'login' }
    }
  }
  if (store.mustChangePassword && to.name !== 'change-password') {
    return { name: 'change-password' }
  }
  const permCode = to.meta.permCode as string | undefined
  if (permCode && !store.has(permCode)) {
    return { name: 'home' }
  }
  return true
})

export default router
