import { del, get, post, put } from './request'
import type { PageResult } from './request'
import type { Id, CreatedAccount, EnableStatus, TenantFeature, TenantItem } from './types'

export interface TenantQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: EnableStatus
}

export interface TenantSaveRequest {
  name: string
  contactName?: string
  contactPhone?: string
  country?: string
  province?: string
  city?: string
  remark?: string
  authCodeChannel?: string
  modelIds?: Id[]
  adminAccount?: string
  adminNickname?: string
  menuKeys?: string[]
}

export const pageTenants = (query: TenantQuery) => get<PageResult<TenantItem>>('/api/tenants', query)
export const tenantDetail = (id: Id) => get<TenantItem>(`/api/tenants/${id}`)
export const createTenant = (data: TenantSaveRequest) => post<CreatedAccount>('/api/tenants', data)
export const updateTenant = (id: Id, data: TenantSaveRequest) => put<void>(`/api/tenants/${id}`, data)
export const toggleTenant = (id: Id, status: EnableStatus) =>
  post<void>(`/api/tenants/${id}/status`, { status })
export const deleteTenant = (id: Id) => del<void>(`/api/tenants/${id}`)
export const fetchFeatures = (id: Id) => get<TenantFeature>(`/api/tenants/${id}/features`)
export const fetchFeatureOptions = () =>
  get<TenantFeature['options']>('/api/tenants/feature-options')
export const saveFeatures = (id: Id, menuKeys: string[]) =>
  put<TenantFeature>(`/api/tenants/${id}/features`, { menuKeys })
export const exportTenants = (query: TenantQuery) => get<string>('/api/tenants/export', query)
