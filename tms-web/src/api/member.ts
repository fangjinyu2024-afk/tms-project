import { del, get, post, put } from './request'
import type { PageResult } from './request'
import type { Id, CreatedAccount, EnableStatus, MemberItem } from './types'

export interface MemberQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  orgId?: Id
  status?: EnableStatus
}

export interface MemberSaveRequest {
  orgId?: Id
  account?: string
  nickname: string
  email?: string
  phone?: string
  roleIds: Id[]
}

export const pageMembers = (query: MemberQuery) => get<PageResult<MemberItem>>('/api/members', query)
export const memberDetail = (id: Id) => get<MemberItem>(`/api/members/${id}`)
export const createMember = (data: MemberSaveRequest) => post<CreatedAccount>('/api/members', data)
export const updateMember = (id: Id, data: MemberSaveRequest) => put<void>(`/api/members/${id}`, data)
export const toggleMember = (id: Id, status: EnableStatus) =>
  post<void>(`/api/members/${id}/status`, { status })
export const deleteMember = (id: Id) => del<void>(`/api/members/${id}`)
export const resetMemberPassword = (id: Id) =>
  post<CreatedAccount>(`/api/members/${id}/password/reset`)
export const assignRoles = (id: Id, roleIds: Id[]) =>
  put<void>(`/api/members/${id}/roles`, { roleIds })
export const exportMembers = (query: MemberQuery) => get<string>('/api/members/export', query)
