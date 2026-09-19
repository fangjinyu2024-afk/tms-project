import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { ACCEPT_LANGUAGE, currentLocale, translate } from '@/i18n'
import router from '@/router'

/** 统一响应体，结构见详细设计 5.2 */
export interface Result<T> {
  code: string
  message: string
  data: T
  traceId: string
}

export interface PageResult<T> {
  total: number
  pageNum: number
  pageSize: number
  list: T[]
}

const http: AxiosInstance = axios.create({
  baseURL: '/',
  timeout: 30000
})

http.interceptors.request.use((config) => {
  const token = useUserStore().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  config.headers['Accept-Language'] = ACCEPT_LANGUAGE[currentLocale()]
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const body = error.response?.data as Result<unknown> | undefined
    const message = body?.message || translate('error.requestFailed')
    if (status === 401) {
      useUserStore().clear()
      void router.push({ name: 'login' })
    } else if (body?.code === 'AUTH_005') {
      void router.push({ name: 'change-password' })
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await http.request<Result<T>>(config)
  return response.data.data
}

export const get = <T>(url: string, params?: unknown) => request<T>({ url, method: 'GET', params })
export const post = <T>(url: string, data?: unknown) => request<T>({ url, method: 'POST', data })
export const put = <T>(url: string, data?: unknown) => request<T>({ url, method: 'PUT', data })
export const del = <T>(url: string) => request<T>({ url, method: 'DELETE' })
