import { defineStore } from 'pinia'

interface UserInfo {
  userId: number
  displayName: string
  phone?: string
  avatarUrl?: string
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: (localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user') as string) : null) as UserInfo | null,
    shouldShowLogin: false,
    returnPath: null as string | null // 登录成功后需要返回的路由路径
  }),
  getters: {
    isLoggedIn: (s) => !!s.token
  },
  actions: {
    setAuth(token: string, user: UserInfo) {
      this.token = token
      this.user = user
      this.shouldShowLogin = false
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
    requireLogin(returnPath?: string) {
      this.logout()
      this.shouldShowLogin = true
      // 保存当前路由路径，登录成功后返回
      if (returnPath) {
        this.returnPath = returnPath
      }
    },
    clearLoginRequirement() {
      this.shouldShowLogin = false
    },
    getAndClearReturnPath() {
      const path = this.returnPath
      this.returnPath = null
      return path
    }
  }
})


