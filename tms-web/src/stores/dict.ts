/** 枚举取值，与 docs/TMS-详细设计.md 第 6 章一致；名称走 i18n，筛选项按完整取值渲染，不按当前页数据推导 */

export interface DictOption {
  value: string
  labelKey: string
}

function options(prefix: string, values: string[]): DictOption[] {
  return values.map((value) => ({ value, labelKey: `${prefix}.${value}` }))
}

export const ENABLE_STATUS = options('enums.enableStatus', ['ENABLED', 'DISABLED'])
export const DATA_SCOPE = options('enums.dataScope', ['SELF_ORG', 'ORG_AND_SUB'])
export const SESSION_ENTRY = options('enums.sessionEntry', ['CONSOLE', 'TOOL'])
export const LOGIN_RESULT = options('enums.loginResult', ['SUCCESS', 'FAIL'])
export const OPER_RESULT = options('enums.operResult', ['SUCCESS', 'PARTIAL', 'FAIL'])
export const PRODUCT_CATEGORY = options('enums.productCategory', ['TRADITIONAL_POS', 'DESKTOP_POS'])
export const AUTH_CODE_CHANNEL = options('enums.authCodeChannel', ['TOOL', 'EMAIL'])

export const LOG_MODULE = options('enums.logModule', [
  'DEVICE', 'DEVICE_GROUP', 'DEVICE_FLOW', 'MODE_TASK', 'PACKAGE', 'OTA_TASK', 'KEY', 'RKI_TASK',
  'ACTIVATION_GRANT', 'AUTH_CODE', 'ACTIVATION', 'CA', 'DEVICE_CERT', 'SERVER_CERT', 'TENANT',
  'ORG', 'MEMBER', 'ROLE', 'PRODUCT', 'SESSION', 'AUTH'
])

export const OPER_ACTION = options('enums.operAction', [
  'CREATE', 'UPDATE', 'DELETE', 'IMPORT', 'EXPORT', 'PUBLISH', 'STOP', 'TOGGLE', 'AUTHORIZE',
  'ASSIGN_ROLE', 'RESET_PASSWORD', 'FORCE_LOGOUT', 'REVOKE', 'GENERATE', 'SIGN', 'PROVISION',
  'VIEW_SENSITIVE'
])
