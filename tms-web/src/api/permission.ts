import { get } from './request'
import type { Id, PermissionCatalogItem } from './types'

export const fetchCatalog = (orgId?: Id) =>
  get<PermissionCatalogItem[]>('/api/permissions/catalog', { orgId })

export const fetchGrantable = (orgId?: Id) =>
  get<string[]>('/api/permissions/grantable', { orgId })
