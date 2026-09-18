import { defineStore } from 'pinia'

/** 枚举取值，与 docs/TMS-详细设计.md 第 6 章一致；筛选项按完整取值渲染，不按当前页数据推导 */
export const ENABLE_STATUS = [
  { value: 'ENABLED', label: '启用' },
  { value: 'DISABLED', label: '停用' }
]

export const DATA_SCOPE = [
  { value: 'SELF_ORG', label: '仅成员所属机构' },
  { value: 'ORG_AND_SUB', label: '成员所属机构及下级机构' }
]

export const SESSION_ENTRY = [
  { value: 'CONSOLE', label: '管理后台' },
  { value: 'TOOL', label: '激活工具' }
]

export const LOGIN_RESULT = [
  { value: 'SUCCESS', label: '成功' },
  { value: 'FAIL', label: '失败' }
]

export const OPER_RESULT = [
  { value: 'SUCCESS', label: '成功' },
  { value: 'PARTIAL', label: '部分成功' },
  { value: 'FAIL', label: '失败' }
]

export const PRODUCT_CATEGORY = [
  { value: 'TRADITIONAL_POS', label: '传统 POS' },
  { value: 'DESKTOP_POS', label: '台式 POS' }
]

export const AUTH_CODE_CHANNEL = [
  { value: 'TOOL', label: '后台取码' },
  { value: 'EMAIL', label: '邮箱发送' }
]

export const LOG_MODULE = [
  { value: 'DEVICE', label: '设备管理' },
  { value: 'DEVICE_GROUP', label: '设备分组' },
  { value: 'DEVICE_FLOW', label: '设备流转' },
  { value: 'MODE_TASK', label: '设备模式任务' },
  { value: 'PACKAGE', label: '升级包管理' },
  { value: 'OTA_TASK', label: 'OTA 任务' },
  { value: 'KEY', label: '密钥管理' },
  { value: 'RKI_TASK', label: 'RKI 任务' },
  { value: 'ACTIVATION_GRANT', label: '激活授权' },
  { value: 'AUTH_CODE', label: '授权码' },
  { value: 'ACTIVATION', label: '设备激活' },
  { value: 'CA', label: 'CA 管理' },
  { value: 'DEVICE_CERT', label: '设备证书' },
  { value: 'SERVER_CERT', label: '平台服务证书' },
  { value: 'TENANT', label: '客户管理' },
  { value: 'ORG', label: '机构管理' },
  { value: 'MEMBER', label: '成员管理' },
  { value: 'ROLE', label: '角色管理' },
  { value: 'PRODUCT', label: '产品与型号' },
  { value: 'SESSION', label: '在线会话' },
  { value: 'AUTH', label: '认证与账号安全' }
]

export const OPER_ACTION = [
  { value: 'CREATE', label: '新增' },
  { value: 'UPDATE', label: '编辑' },
  { value: 'DELETE', label: '删除' },
  { value: 'IMPORT', label: '导入' },
  { value: 'EXPORT', label: '导出' },
  { value: 'PUBLISH', label: '发布' },
  { value: 'STOP', label: '停止' },
  { value: 'TOGGLE', label: '启停' },
  { value: 'AUTHORIZE', label: '功能授权' },
  { value: 'ASSIGN_ROLE', label: '角色分配' },
  { value: 'RESET_PASSWORD', label: '重置密码' },
  { value: 'FORCE_LOGOUT', label: '强制下线' },
  { value: 'REVOKE', label: '撤销' },
  { value: 'GENERATE', label: '生成' },
  { value: 'SIGN', label: '签发' },
  { value: 'PROVISION', label: '标记产线预置' },
  { value: 'VIEW_SENSITIVE', label: '敏感信息查看' }
]

export const useDictStore = defineStore('dict', {
  state: () => ({
    enableStatus: ENABLE_STATUS,
    dataScope: DATA_SCOPE,
    sessionEntry: SESSION_ENTRY,
    productCategory: PRODUCT_CATEGORY,
    logModule: LOG_MODULE,
    operAction: OPER_ACTION
  })
})
