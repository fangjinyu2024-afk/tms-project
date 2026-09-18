import { get, post } from './request'
import type { PageResult } from './request'
import type { Id, LoginLogItem, OperLogItem, SessionItem } from './types'

export interface OperLogQuery {
  pageNum?: number
  pageSize?: number
  module?: string
  action?: string
  result?: string
  keyword?: string
  startTime?: string
  endTime?: string
}

export interface LoginLogQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  result?: string
  entry?: string
  startTime?: string
  endTime?: string
}

export interface SessionQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  entry?: string
  orgId?: Id
}

export const pageOperLogs = (query: OperLogQuery) => get<PageResult<OperLogItem>>('/api/oper-logs', query)
export const operLogDetail = (id: Id) => get<OperLogItem>(`/api/oper-logs/${id}`)
export const exportOperLogs = (query: OperLogQuery) => get<string>('/api/oper-logs/export', query)

export const pageLoginLogs = (query: LoginLogQuery) =>
  get<PageResult<LoginLogItem>>('/api/login-logs', query)
export const exportLoginLogs = (query: LoginLogQuery) => get<string>('/api/login-logs/export', query)

export const pageSessions = (query: SessionQuery) => get<PageResult<SessionItem>>('/api/sessions', query)
export const forceLogout = (id: Id, reason: string) =>
  post<void>(`/api/sessions/${id}/force-logout`, { reason })
export const exportSessions = (query: SessionQuery) => get<string>('/api/sessions/export', query)
