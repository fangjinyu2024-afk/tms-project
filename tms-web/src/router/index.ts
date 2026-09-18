import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/layouts/MainLayout.vue'

/**
 * 路由元数据绑定菜单键与权限码，菜单按有效权限渲染；
 * 业务模块（设备、远程维护、激活与证书）随对应章节的详细设计补充。
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/change-password',
    name: 'change-password',
    component: () => import('@/views/ChangePasswordView.vue'),
    meta: { title: '修改密码' }
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: '工作台', menuKey: 'home', group: '工作台', icon: 'HomeFilled' }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/ProfileView.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'system/customers',
        name: 'customers',
        component: () => import('@/views/system/CustomerListView.vue'),
        meta: { title: '客户管理', menuKey: 'customers', group: '系统管理', permCode: 'customers:view' }
      },
      {
        path: 'system/orgs',
        name: 'orgs',
        component: () => import('@/views/system/OrgListView.vue'),
        meta: { title: '机构管理', menuKey: 'orgs', group: '系统管理', permCode: 'orgs:view' }
      },
      {
        path: 'system/members',
        name: 'members',
        component: () => import('@/views/system/MemberListView.vue'),
        meta: { title: '成员管理', menuKey: 'members', group: '系统管理', permCode: 'members:view' }
      },
      {
        path: 'system/roles',
        name: 'roles',
        component: () => import('@/views/system/RoleListView.vue'),
        meta: { title: '角色管理', menuKey: 'roles', group: '系统管理', permCode: 'roles:view' }
      },
      {
        path: 'system/products',
        name: 'products',
        component: () => import('@/views/system/ProductListView.vue'),
        meta: { title: '产品与型号', menuKey: 'products', group: '系统管理', permCode: 'products:view' }
      },
      {
        path: 'system/oper-logs',
        name: 'logs',
        component: () => import('@/views/system/OperLogView.vue'),
        meta: { title: '操作日志', menuKey: 'logs', group: '系统管理', permCode: 'logs:view' }
      },
      {
        path: 'system/login-logs',
        name: 'logins',
        component: () => import('@/views/system/LoginLogView.vue'),
        meta: { title: '登录日志', menuKey: 'logins', group: '系统管理', permCode: 'logins:view' }
      },
      {
        path: 'system/sessions',
        name: 'sessions',
        component: () => import('@/views/system/SessionListView.vue'),
        meta: { title: '在线会话', menuKey: 'sessions', group: '系统管理', permCode: 'sessions:view' }
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
