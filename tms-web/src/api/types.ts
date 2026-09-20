/** 接口模型，字段与 docs/TMS-详细设计.md 第 5 章保持一致 */

export type EnableStatus = 'ENABLED' | 'DISABLED'
export type DataScope = 'SELF_ORG' | 'ORG_AND_SUB'
export type SessionEntry = 'CONSOLE' | 'TOOL'

/** 主键为雪花算法生成的 64 位整数，接口以字符串传输，避免 JavaScript 精度丢失 */
export type Id = string

export interface MemberProfile {
  memberId: Id
  account: string
  nickname: string
  email?: string
  emailVerified?: boolean
  phone?: string
  tenantId: Id
  tenantName?: string
  orgId: Id
  orgName?: string
  orgPath: string
  platform: boolean
}

export interface Captcha {
  captchaId: string
  /** base64 图片，形如 data:image/png;base64,... */
  image: string
}

export interface LoginResponse {
  token: string
  expiresIn: number
  mustChangePassword: boolean
  member: MemberProfile
  menus: string[]
  permissions: Record<string, DataScope>
}

export interface Profile {
  member: MemberProfile
  menus: string[]
  permissions: Record<string, DataScope>
  mustChangePassword: boolean
}

export interface OrgTreeNode {
  id: Id
  parentId: Id
  name: string
  orgType: string
  status: EnableStatus
  orgPath: string
  subCount: number
  children: OrgTreeNode[]
}

export interface OrgItem {
  id: Id
  tenantId: Id
  parentId: Id
  parentName?: string
  orgPath: string
  orgType: string
  orgTypeLabel: string
  name: string
  contactName?: string
  contactPhone?: string
  status: EnableStatus
  memberCount: number
  subOrgCount: number
  createTime: string
}

export interface RoleOption {
  id: Id
  name: string
  ownerOrgName?: string
  dataScope: DataScope
  builtin: boolean
  assignable?: boolean
}

export interface RoleItem {
  id: Id
  name: string
  description?: string
  ownerOrgId: Id
  ownerOrgName?: string
  dataScope: DataScope
  dataScopeLabel: string
  builtin: boolean
  status: EnableStatus
  version: number
  memberCount: number
  permCodes?: string[]
  createTime: string
}

export interface PermissionCatalogItem {
  groupName: string
  menuKey: string
  menuName: string
  actions: Array<{ permCode: string; action: string; actionName: string; grantable: boolean }>
}

export interface MemberItem {
  id: Id
  orgId: Id
  orgName?: string
  account: string
  nickname: string
  email?: string
  emailVerified?: boolean
  phone?: string
  status: EnableStatus
  mustChangePassword: boolean
  roles: RoleOption[]
  createTime: string
}

export interface TenantItem {
  id: Id
  name: string
  rootOrgId: Id
  contactName?: string
  contactPhone?: string
  country?: string
  province?: string
  city?: string
  remark?: string
  status: EnableStatus
  authCodeChannel: string
  modelIds?: Id[]
  modelNames?: string[]
  menuKeys?: string[]
  createTime: string
}

export interface TenantFeature {
  tenantId: Id
  menuKeys: string[]
  added?: string[]
  removed?: string[]
  options: Array<{ menuKey: string; menuName: string; groupName: string; required: boolean }>
}

export interface ProductModel {
  id?: Id
  productId?: Id
  productName?: string
  model: string
}

export interface ProductItem {
  id: Id
  category: string
  categoryLabel: string
  name: string
  imagePath?: string
  description?: string
  models: ProductModel[]
  createTime: string
}

export interface ModelOption {
  id: Id
  model: string
  productName?: string
  category?: string
}

export interface OperLogItem {
  id: Id
  module: string
  moduleLabel: string
  action: string
  actionLabel: string
  objectType?: string
  objectId?: string
  objectName?: string
  result: string
  resultLabel: string
  failReason?: string
  totalCount?: number
  successCount?: number
  changeSummary?: string
  account: string
  nickname: string
  clientIp?: string
  traceId: string
  operTime: string
}

export interface LoginLogItem {
  id: Id
  account: string
  entry: SessionEntry
  entryLabel: string
  result: string
  resultLabel: string
  failReason?: string
  clientIp: string
  userAgent?: string
  loginTime: string
}

export interface SessionItem {
  id: Id
  memberId: Id
  account: string
  nickname: string
  orgId: Id
  orgName?: string
  entry: SessionEntry
  entryLabel: string
  clientIp: string
  userAgent?: string
  loginTime: string
  lastActiveTime: string
  current: boolean
}

export interface CreatedAccount {
  memberId?: Id
  orgId?: Id
  tenantId?: Id
  account?: string
  adminAccount?: string
  initialPassword?: string
}
