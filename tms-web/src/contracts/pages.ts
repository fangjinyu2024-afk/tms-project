/**
 * 页面契约：列、列序、筛选项、按钮与说明文字的唯一来源。
 *
 * 由 scripts/extract-contracts.mjs 从 prototype/TMS-原型.html 的 configs{} 抽取，**不要手工编辑**。
 * 复刻页面时从这里取，不要各自去读原型再转述——那是多人协作时页面跑偏的主要原因。
 *
 * prototypeLabel 是原型里的中文原文，仅用于比对核验；页面渲染一律用 labelKey 走 i18n。
 * 筛选项的取值以 stores/dict.ts 的枚举为准，prototypeOptions 只说明原型提供了哪几项。
 */

export interface ContractColumn {
  key: string
  labelKey: string
  prototypeLabel: string
}

export interface ContractFilter {
  field: string
  labelKey: string
  prototypeLabel: string
  prototypeOptions?: string[]
}

export interface PageContract {
  menuKey: string
  descriptionKey: string
  prototypeDescription: string
  createLabelKey?: string
  prototypeCreateLabel?: string
  columns: ContractColumn[]
  filters: ContractFilter[]
}

export const PAGE_CONTRACTS: Record<string, PageContract> = {
  'activations': {
    menuKey: 'activations',
    descriptionKey: 'page.activations.description',
    prototypeDescription: '查询设备的工厂激活、出厂激活和防拆激活结果。',
    columns: [
      { key: 'sn', labelKey: 'page.activations.col.sn', prototypeLabel: '设备SN' },
      { key: 'model', labelKey: 'page.activations.col.model', prototypeLabel: '型号' },
      { key: 'type', labelKey: 'page.activations.col.type', prototypeLabel: '激活类型' },
      { key: 'operator', labelKey: 'page.activations.col.operator', prototypeLabel: '操作人' },
      { key: 'reason', labelKey: 'page.activations.col.reason', prototypeLabel: '自毁／防拆原因' },
      { key: 'status', labelKey: 'page.activations.col.status', prototypeLabel: '激活结果' },
      { key: 'created', labelKey: 'page.activations.col.created', prototypeLabel: '激活时间' },
    ],
    filters: [
      { field: 'type', labelKey: 'page.activations.filter.type', prototypeLabel: '激活类型', prototypeOptions: ['工厂激活', '出厂激活', '防拆激活'] },
    ],
  },
  'cas': {
    menuKey: 'cas',
    descriptionKey: 'page.cas.description',
    prototypeDescription: '管理激活、RKI 和 TLS 三类 CA 密钥；终端出厂需预置对应公钥。',
    createLabelKey: 'page.cas.create',
    prototypeCreateLabel: '生成／导入CA',
    columns: [
      { key: 'purpose', labelKey: 'page.cas.col.purpose', prototypeLabel: 'CA用途' },
      { key: 'name', labelKey: 'page.cas.col.name', prototypeLabel: 'CA名称' },
      { key: 'algorithm', labelKey: 'page.cas.col.algorithm', prototypeLabel: '密钥算法' },
      { key: 'fingerprint', labelKey: 'page.cas.col.fingerprint', prototypeLabel: '公钥指纹' },
      { key: 'expires', labelKey: 'page.cas.col.expires', prototypeLabel: '有效期至' },
      { key: 'status', labelKey: 'page.cas.col.status', prototypeLabel: '状态' },
    ],
    filters: [
      { field: 'purpose', labelKey: 'page.cas.filter.purpose', prototypeLabel: 'CA用途', prototypeOptions: ['激活CA', 'RKI CA', 'TLS CA'] },
    ],
  },
  'certs': {
    menuKey: 'certs',
    descriptionKey: 'page.certs.description',
    prototypeDescription: '查询设备的身份、RKI 和 TLS 三类证书及有效期。',
    columns: [
      { key: 'sn', labelKey: 'page.certs.col.sn', prototypeLabel: '设备SN' },
      { key: 'model', labelKey: 'page.certs.col.model', prototypeLabel: '型号' },
      { key: 'org', labelKey: 'page.certs.col.org', prototypeLabel: '所属机构' },
      { key: 'purpose', labelKey: 'page.certs.col.purpose', prototypeLabel: '证书用途' },
      { key: 'signedBy', labelKey: 'page.certs.col.signedBy', prototypeLabel: '签发CA' },
      { key: 'serial', labelKey: 'page.certs.col.serial', prototypeLabel: '序列号' },
      { key: 'expires', labelKey: 'page.certs.col.expires', prototypeLabel: '有效期至' },
      { key: 'status', labelKey: 'page.certs.col.status', prototypeLabel: '状态' },
    ],
    filters: [
      { field: 'purpose', labelKey: 'page.certs.filter.purpose', prototypeLabel: '证书用途', prototypeOptions: ['设备身份证书', 'RKI证书', 'TLS证书'] },
    ],
  },
  'codes': {
    menuKey: 'codes',
    descriptionKey: 'page.codes.description',
    prototypeDescription: '查询激活授权码的发放和使用情况。',
    createLabelKey: 'page.codes.create',
    prototypeCreateLabel: '申请授权码',
    columns: [
      { key: 'account', labelKey: 'page.codes.col.account', prototypeLabel: '申请账户' },
      { key: 'sn', labelKey: 'page.codes.col.sn', prototypeLabel: '绑定设备SN' },
      { key: 'code', labelKey: 'page.codes.col.code', prototypeLabel: '授权码' },
      { key: 'created', labelKey: 'page.codes.col.created', prototypeLabel: '申请时间' },
      { key: 'expires', labelKey: 'page.codes.col.expires', prototypeLabel: '过期时间' },
      { key: 'consumedAt', labelKey: 'page.codes.col.consumedAt', prototypeLabel: '使用时间' },
      { key: 'status', labelKey: 'page.codes.col.status', prototypeLabel: '授权码状态' },
    ],
    filters: [
      { field: 'status', labelKey: 'page.codes.filter.status', prototypeLabel: '状态', prototypeOptions: ['待使用', '已使用', '已过期', '已撤销'] },
    ],
  },
  'customers': {
    menuKey: 'customers',
    descriptionKey: 'page.customers.description',
    prototypeDescription: '维护客户信息及其可使用的产品型号。',
    createLabelKey: 'page.customers.create',
    prototypeCreateLabel: '新增客户',
    columns: [
      { key: 'name', labelKey: 'page.customers.col.name', prototypeLabel: '客户名称' },
      { key: 'contact', labelKey: 'page.customers.col.contact', prototypeLabel: '联系人' },
      { key: 'phone', labelKey: 'page.customers.col.phone', prototypeLabel: '联系电话' },
      { key: 'country', labelKey: 'page.customers.col.country', prototypeLabel: '国家／地区' },
      { key: 'models', labelKey: 'page.customers.col.models', prototypeLabel: '关联型号' },
      { key: 'admin', labelKey: 'page.customers.col.admin', prototypeLabel: '管理员账号' },
      { key: 'status', labelKey: 'page.customers.col.status', prototypeLabel: '状态' },
    ],
    filters: [
      { field: 'country', labelKey: 'page.customers.filter.country', prototypeLabel: '国家／地区' },
    ],
  },
  'grants': {
    menuKey: 'grants',
    descriptionKey: 'page.grants.description',
    prototypeDescription: '设置激活人员可激活的设备范围、额度和有效期。',
    createLabelKey: 'page.grants.create',
    prototypeCreateLabel: '添加激活授权',
    columns: [
      { key: 'name', labelKey: 'page.grants.col.name', prototypeLabel: '授权名称' },
      { key: 'member', labelKey: 'page.grants.col.member', prototypeLabel: '用户账户' },
      { key: 'scope', labelKey: 'page.grants.col.scope', prototypeLabel: '设备范围' },
      { key: 'quota', labelKey: 'page.grants.col.quota', prototypeLabel: '累计激活额度' },
      { key: 'used', labelKey: 'page.grants.col.used', prototypeLabel: '已发起会话' },
      { key: 'expires', labelKey: 'page.grants.col.expires', prototypeLabel: '有效期至' },
      { key: 'status', labelKey: 'page.grants.col.status', prototypeLabel: '状态' },
    ],
    filters: [
      { field: 'status', labelKey: 'page.grants.filter.status', prototypeLabel: '状态', prototypeOptions: ['启用', '停用'] },
    ],
  },
  'groups': {
    menuKey: 'groups',
    descriptionKey: 'page.groups.description',
    prototypeDescription: '把设备编成分组，便于批量创建升级任务。',
    createLabelKey: 'page.groups.create',
    prototypeCreateLabel: '新建分组',
    columns: [
      { key: 'name', labelKey: 'page.groups.col.name', prototypeLabel: '分组名称' },
      { key: 'org', labelKey: 'page.groups.col.org', prototypeLabel: '所属机构' },
      { key: 'count', labelKey: 'page.groups.col.count', prototypeLabel: '设备数量' },
      { key: 'remark', labelKey: 'page.groups.col.remark', prototypeLabel: '备注' },
      { key: 'created', labelKey: 'page.groups.col.created', prototypeLabel: '创建时间' },
    ],
    filters: [
      { field: 'org', labelKey: 'page.groups.filter.org', prototypeLabel: '所属机构' },
    ],
  },
  'keys': {
    menuKey: 'keys',
    descriptionKey: 'page.keys.description',
    prototypeDescription: '管理支付密钥，页面不展示密钥内容。',
    createLabelKey: 'page.keys.create',
    prototypeCreateLabel: '导入密钥',
    columns: [
      { key: 'typeName', labelKey: 'page.keys.col.typeName', prototypeLabel: '密钥类型' },
      { key: 'index', labelKey: 'page.keys.col.index', prototypeLabel: '密钥索引' },
      { key: 'status', labelKey: 'page.keys.col.status', prototypeLabel: '状态' },
      { key: 'created', labelKey: 'page.keys.col.created', prototypeLabel: '导入时间' },
    ],
    filters: [
      { field: 'system', labelKey: 'page.keys.filter.system', prototypeLabel: '密钥体系', prototypeOptions: ['MK/SK', 'DUKPT', 'Fixed Key'] },
    ],
  },
  'logins': {
    menuKey: 'logins',
    descriptionKey: 'page.logins.description',
    prototypeDescription: '查询账号登录记录。',
    columns: [
      { key: 'created', labelKey: 'page.logins.col.created', prototypeLabel: '登录时间' },
      { key: 'account', labelKey: 'page.logins.col.account', prototypeLabel: '账号' },
      { key: 'org', labelKey: 'page.logins.col.org', prototypeLabel: '所属机构' },
      { key: 'status', labelKey: 'page.logins.col.status', prototypeLabel: '登录结果' },
      { key: 'reason', labelKey: 'page.logins.col.reason', prototypeLabel: '失败原因' },
      { key: 'ip', labelKey: 'page.logins.col.ip', prototypeLabel: '来源IP' },
      { key: 'browser', labelKey: 'page.logins.col.browser', prototypeLabel: '浏览器／系统' },
    ],
    filters: [
      { field: 'status', labelKey: 'page.logins.filter.status', prototypeLabel: '登录结果', prototypeOptions: ['成功', '失败'] },
    ],
  },
  'logs': {
    menuKey: 'logs',
    descriptionKey: 'page.logs.description',
    prototypeDescription: '查询系统操作记录。',
    columns: [
      { key: 'created', labelKey: 'page.logs.col.created', prototypeLabel: '操作时间' },
      { key: 'operator', labelKey: 'page.logs.col.operator', prototypeLabel: '操作人' },
      { key: 'org', labelKey: 'page.logs.col.org', prototypeLabel: '所属机构' },
      { key: 'module', labelKey: 'page.logs.col.module', prototypeLabel: '业务模块' },
      { key: 'action', labelKey: 'page.logs.col.action', prototypeLabel: '操作类型' },
      { key: 'name', labelKey: 'page.logs.col.name', prototypeLabel: '操作内容' },
      { key: 'status', labelKey: 'page.logs.col.status', prototypeLabel: '执行结果' },
      { key: 'ip', labelKey: 'page.logs.col.ip', prototypeLabel: '来源IP' },
    ],
    filters: [
      { field: 'module', labelKey: 'page.logs.filter.module', prototypeLabel: '业务模块' },
    ],
  },
  'members': {
    menuKey: 'members',
    descriptionKey: 'page.members.description',
    prototypeDescription: '管理登录账号，通过角色分配权限。',
    createLabelKey: 'page.members.create',
    prototypeCreateLabel: '创建成员',
    columns: [
      { key: 'account', labelKey: 'page.members.col.account', prototypeLabel: '账号' },
      { key: 'name', labelKey: 'page.members.col.name', prototypeLabel: '昵称' },
      { key: 'email', labelKey: 'page.members.col.email', prototypeLabel: '邮箱' },
      { key: 'org', labelKey: 'page.members.col.org', prototypeLabel: '所属机构' },
      { key: 'roles', labelKey: 'page.members.col.roles', prototypeLabel: '角色' },
      { key: 'status', labelKey: 'page.members.col.status', prototypeLabel: '状态' },
      { key: 'last', labelKey: 'page.members.col.last', prototypeLabel: '最近登录' },
    ],
    filters: [
      { field: 'status', labelKey: 'page.members.filter.status', prototypeLabel: '状态', prototypeOptions: ['启用', '停用'] },
    ],
  },
  'operations': {
    menuKey: 'operations',
    descriptionKey: 'page.operations.description',
    prototypeDescription: '查询设备入库、出库、转移、回收和报废记录。',
    columns: [
      { key: 'batch', labelKey: 'page.operations.col.batch', prototypeLabel: '批次号' },
      { key: 'type', labelKey: 'page.operations.col.type', prototypeLabel: '操作类型' },
      { key: 'org', labelKey: 'page.operations.col.org', prototypeLabel: '来源机构' },
      { key: 'target', labelKey: 'page.operations.col.target', prototypeLabel: '目标机构' },
      { key: 'total', labelKey: 'page.operations.col.total', prototypeLabel: '总数' },
      { key: 'success', labelKey: 'page.operations.col.success', prototypeLabel: '成功' },
      { key: 'fail', labelKey: 'page.operations.col.fail', prototypeLabel: '失败' },
      { key: 'operator', labelKey: 'page.operations.col.operator', prototypeLabel: '操作人' },
      { key: 'created', labelKey: 'page.operations.col.created', prototypeLabel: '操作时间' },
    ],
    filters: [
      { field: 'type', labelKey: 'page.operations.filter.type', prototypeLabel: '操作类型', prototypeOptions: ['号段入库', '批量入库', '设备出库', '设备转移', '退货回收', '设备报废'] },
    ],
  },
  'orgs': {
    menuKey: 'orgs',
    descriptionKey: 'page.orgs.description',
    prototypeDescription: '管理本机构及下级机构。',
    createLabelKey: 'page.orgs.create',
    prototypeCreateLabel: '新增子机构',
    columns: [
      { key: 'name', labelKey: 'page.orgs.col.name', prototypeLabel: '机构名称' },
      { key: 'parent', labelKey: 'page.orgs.col.parent', prototypeLabel: '上级机构' },
      { key: 'contact', labelKey: 'page.orgs.col.contact', prototypeLabel: '联系人' },
      { key: 'phone', labelKey: 'page.orgs.col.phone', prototypeLabel: '联系方式' },
      { key: 'admin', labelKey: 'page.orgs.col.admin', prototypeLabel: '管理员账号' },
      { key: 'status', labelKey: 'page.orgs.col.status', prototypeLabel: '状态' },
    ],
    filters: [
      { field: 'status', labelKey: 'page.orgs.filter.status', prototypeLabel: '状态', prototypeOptions: ['启用', '停用'] },
    ],
  },
  'packages': {
    menuKey: 'packages',
    descriptionKey: 'page.packages.description',
    prototypeDescription: '管理 Kernel、应用和参数文件的版本。',
    createLabelKey: 'page.packages.create',
    prototypeCreateLabel: '上传升级包',
    columns: [
      { key: 'name', labelKey: 'page.packages.col.name', prototypeLabel: '升级包名称' },
      { key: 'type', labelKey: 'page.packages.col.type', prototypeLabel: '类型' },
      { key: 'model', labelKey: 'page.packages.col.model', prototypeLabel: '适配型号' },
      { key: 'version', labelKey: 'page.packages.col.version', prototypeLabel: '版本' },
      { key: 'org', labelKey: 'page.packages.col.org', prototypeLabel: '维护机构' },
      { key: 'size', labelKey: 'page.packages.col.size', prototypeLabel: '文件大小' },
      { key: 'status', labelKey: 'page.packages.col.status', prototypeLabel: '状态' },
    ],
    filters: [
      { field: 'type', labelKey: 'page.packages.filter.type', prototypeLabel: '升级类型', prototypeOptions: ['Kernel', 'APP', '参数文件'] },
    ],
  },
  'products': {
    menuKey: 'products',
    descriptionKey: 'page.products.description',
    prototypeDescription: '维护产品及其型号。',
    createLabelKey: 'page.products.create',
    prototypeCreateLabel: '创建产品',
    columns: [
      { key: 'name', labelKey: 'page.products.col.name', prototypeLabel: '产品名称' },
      { key: 'category', labelKey: 'page.products.col.category', prototypeLabel: '产品类别' },
      { key: 'models', labelKey: 'page.products.col.models', prototypeLabel: '产品型号' },
      { key: 'remark', labelKey: 'page.products.col.remark', prototypeLabel: '描述' },
      { key: 'created', labelKey: 'page.products.col.created', prototypeLabel: '创建时间' },
    ],
    filters: [
      { field: 'category', labelKey: 'page.products.filter.category', prototypeLabel: '产品类别', prototypeOptions: ['传统POS', '台式POS'] },
    ],
  },
  'roles': {
    menuKey: 'roles',
    descriptionKey: 'page.roles.description',
    prototypeDescription: '配置角色可用的功能，以及成员持有该角色时能看到哪些机构的数据。',
    createLabelKey: 'page.roles.create',
    prototypeCreateLabel: '新增角色',
    columns: [
      { key: 'name', labelKey: 'page.roles.col.name', prototypeLabel: '角色名称' },
      { key: 'org', labelKey: 'page.roles.col.org', prototypeLabel: '归属机构' },
      { key: 'scope', labelKey: 'page.roles.col.scope', prototypeLabel: '可管理范围' },
      { key: 'remark', labelKey: 'page.roles.col.remark', prototypeLabel: '描述' },
      { key: 'status', labelKey: 'page.roles.col.status', prototypeLabel: '状态' },
    ],
    filters: [
      { field: 'status', labelKey: 'page.roles.filter.status', prototypeLabel: '状态', prototypeOptions: ['启用', '停用'] },
    ],
  },
  'servercerts': {
    menuKey: 'servercerts',
    descriptionKey: 'page.servercerts.description',
    prototypeDescription: '管理平台各服务使用的证书：设备接入、管理后台 HTTPS 和内部服务。',
    createLabelKey: 'page.servercerts.create',
    prototypeCreateLabel: '签发／导入证书',
    columns: [
      { key: 'purpose', labelKey: 'page.servercerts.col.purpose', prototypeLabel: '证书用途' },
      { key: 'name', labelKey: 'page.servercerts.col.name', prototypeLabel: '证书名称' },
      { key: 'subject', labelKey: 'page.servercerts.col.subject', prototypeLabel: '服务主体' },
      { key: 'signedBy', labelKey: 'page.servercerts.col.signedBy', prototypeLabel: '签发方' },
      { key: 'expires', labelKey: 'page.servercerts.col.expires', prototypeLabel: '有效期至' },
      { key: 'status', labelKey: 'page.servercerts.col.status', prototypeLabel: '状态' },
    ],
    filters: [
      { field: 'purpose', labelKey: 'page.servercerts.filter.purpose', prototypeLabel: '证书用途', prototypeOptions: ['设备接入服务', '管理后台HTTPS', '内部服务TLS'] },
    ],
  },
}

/** 自定义渲染的页面，原型里没有 configs 条目，契约随各自页面复刻时补入这里。 */
export const BESPOKE_PAGES = [
  'home', 'devices', 'mode', 'ota', 'ota-records', 'rki', 'rki-records', 'sessions',
] as const
