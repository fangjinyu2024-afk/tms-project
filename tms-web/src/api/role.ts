import { del, get, post, put } from './request'
import type { PageResult } from './request'
import type { Id, DataScope, EnableStatus, RoleItem, RoleOption } from './types'

export interface RoleQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: EnableStatus
  orgId?: Id
}

export interface RoleSaveRequest {
  name: string
  description?: string
  ownerOrgId?: Id
  dataScope: DataScope
  permCodes: string[]
  version?: number
}

export const pageRoles = (query: RoleQuery) => get<PageResult<RoleItem>>('/api/roles', query)
export const roleDetail = (id: Id) => get<RoleItem>(`/api/roles/${id}`)
export const createRole = (data: RoleSaveRequest) => post<Id>('/api/roles', data)
export const updateRole = (id: Id, data: RoleSaveRequest) => put<void>(`/api/roles/${id}`, data)
export const copyRole = (id: Id) => post<Id>(`/api/roles/${id}/copy`)
export const toggleRole = (id: Id, status: EnableStatus) =>
  post<void>(`/api/roles/${id}/status`, { status })
export const deleteRole = (id: Id) => del<void>(`/api/roles/${id}`)
export const roleOptions = (orgId: Id) => get<RoleOption[]>('/api/roles/options', { orgId })
export const exportRoles = (query: RoleQuery) => get<string>('/api/roles/export', query)
