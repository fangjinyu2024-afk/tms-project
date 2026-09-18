import { defineStore } from 'pinia'
import type { DataScope, MemberProfile } from '@/api/types'
import { fetchProfile, login as loginApi, logout as logoutApi } from '@/api/auth'

const TOKEN_KEY = 'tms-token'

interface UserState {
  token: string
  member: MemberProfile | null
  menus: string[]
  permissions: Record<string, DataScope>
  mustChangePassword: boolean
  loaded: boolean
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    member: null,
    menus: [],
    permissions: {},
    mustChangePassword: false,
    loaded: false
  }),
  getters: {
    /** 是否持有该权限码，前端隐藏菜单不构成权限控制，服务端仍会校验 */
    has: (state) => (permCode: string) => Object.prototype.hasOwnProperty.call(state.permissions, permCode),
    hasMenu: (state) => (menuKey: string) => state.menus.includes(menuKey),
    isPlatform: (state) => state.member?.platform === true
  },
  actions: {
    async login(account: string, password: string) {
      const data = await loginApi({ account, password, entry: 'CONSOLE' })
      this.token = data.token
      this.member = data.member
      this.menus = data.menus
      this.permissions = data.permissions
      this.mustChangePassword = data.mustChangePassword
      this.loaded = true
      localStorage.setItem(TOKEN_KEY, data.token)
      return data
    },
    async loadProfile() {
      const profile = await fetchProfile()
      this.member = profile.member
      this.menus = profile.menus
      this.permissions = profile.permissions
      this.mustChangePassword = profile.mustChangePassword
      this.loaded = true
      return profile
    },
    async logout() {
      try {
        await logoutApi()
      } finally {
        this.clear()
      }
    },
    clear() {
      this.token = ''
      this.member = null
      this.menus = []
      this.permissions = {}
      this.mustChangePassword = false
      this.loaded = false
      localStorage.removeItem(TOKEN_KEY)
    }
  }
})
