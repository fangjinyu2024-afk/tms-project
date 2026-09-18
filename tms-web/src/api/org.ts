import { del, get, post, put } from './request'
import type { PageResult } from './request'
import type { Id, CreatedAccount, EnableStatus, OrgItem, OrgTreeNode } from './types'

export interface OrgQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: EnableStatus
  orgId?: Id
}

export interface OrgSaveRequest {
  parentId?: Id
  name: string
  contactName?: string
  contactPhone?: string
  adminAccount?: string
  adminNickname?: string
  adminRoleIds?: Id[]
}

export const fetchOrgTree = () => get<OrgTreeNode[]>('/api/orgs/tree')
export const fetchOrgSelector = () => get<OrgTreeNode[]>('/api/orgs/selector')
export const pageOrgs = (query: OrgQuery) => get<PageResult<OrgItem>>('/api/orgs', query)
export const orgDetail = (id: Id) => get<OrgItem>(`/api/orgs/${id}`)
export const createOrg = (data: OrgSaveRequest) => post<CreatedAccount>('/api/orgs', data)
export const updateOrg = (id: Id, data: OrgSaveRequest) => put<void>(`/api/orgs/${id}`, data)
export const toggleOrg = (id: Id, status: EnableStatus) =>
  post<void>(`/api/orgs/${id}/status`, { status })
export const deleteOrg = (id: Id) => del<void>(`/api/orgs/${id}`)
export const exportOrgs = (query: OrgQuery) => get<string>('/api/orgs/export', query)
