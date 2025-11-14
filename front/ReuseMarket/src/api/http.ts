import axios from 'axios'
import { useAuthStore } from '../store/auth'
import router from '../router'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080'
})

export const apiBase: string = (http.defaults.baseURL as string) || ''
export function absUrl(u?: string): string {
  if (!u) return ''
  if (/^https?:\/\//i.test(u)) return u
  const base = apiBase.replace(/\/$/, '')
  const path = u.startsWith('/') ? u : `/${u}`
  return `${base}${path}`
}

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    if (!config.headers) {
      config.headers = {} as any
    }
    config.headers['Authorization'] = `Bearer ${auth.token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    // 处理 401 未授权响应
    if (error.response?.status === 401) {
      const auth = useAuthStore()
      // 获取当前路由路径，保存以便登录后返回
      const currentPath = router.currentRoute.value.fullPath
      // 退出登录并触发登录弹窗，保存当前路径
      auth.requireLogin(currentPath)
    }
    return Promise.reject(error)
  }
)

export default http


