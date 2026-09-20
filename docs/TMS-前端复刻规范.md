# TMS 前端页面复刻规范

本文件规定前端页面如何按 `prototype/TMS-原型.html` 复刻为 Vue 页面，供多人／多 AI 并行开发时保持一致。**这里写的是约束和调度信息，不是业务设计**；业务规则在 `docs/TMS-功能清单.md` 与 `docs/TMS-详细设计.md`。

## 1. 权威与仲裁

原型是唯一描述「页面长什么样、怎么交互」的产物，详细设计只写页面职责与接口，不写布局样式。因此按维度分工：

| 维度 | 以什么为准 |
|---|---|
| 页面布局、区块顺序、列与列序、筛选项、按钮文案与位置、弹窗还是抽屉、空态与提示语、配色间距字号 | `prototype/TMS-原型.html` |
| 权限码与数据范围、枚举取值、字段语义、接口契约、状态流转、校验规则 | `docs/TMS-功能清单.md`、`docs/TMS-详细设计.md` |

这条不改变 `CLAUDE.md` 的文档权威优先级：原型仍然排在最后，只是**在文档没有规定的 UI 维度上**由原型填空。凡是原型的业务行为与文档冲突，一律以文档为准。

**复刻不等于照抄。** 发现原型与实际需求有偏差、或设计本身不合理，登记到第 6 章并提出确认，不要静默按原型做，也不要静默改掉。

## 2. 设计系统

### 2.1 颜色令牌

已移植到 `tms-web/src/styles/tokens.css`（颜色来自原型 `:root`，尺度令牌来自 2.2 的统计）：

| 令牌 | 值 | 用途 |
|---|---|---|
| `--blue` | `#1677ff` | 主色：主按钮、选中态、链接 |
| `--blue-dark` | `#0958d9` | 主色深色：渐变终点、悬停 |
| `--ink` | `#1f2c42` | 正文 |
| `--muted` | `#66778d` | 次要文字、说明 |
| `--line` | `#e7ebf2` | 分隔线、面板边框 |
| `--bg` | `#f3f6fa` | 页面底色 |
| `--nav` | `#fff` | 侧栏与顶栏底色 |
| `--green` | `#15865a` | 成功态 |
| `--red` | `#cc3943` | 失败与危险 |
| `--amber` | `#ad700b` | 警示 |

基准字号 14px，字体栈 `Inter, "Segoe UI", "Microsoft YaHei", "PingFang SC", sans-serif`。

### 2.2 尺度约定

从原型统计出的实际用法，新页面照此取值，不要另创：

| 项 | 取值 |
|---|---|
| 圆角 | 输入框与按钮 `5px`，面板与分段控件 `8px`，弹窗 `9px`，徽标与标签 `4px`，图标底块 `6px` |
| 字号 | 表格与说明 `12px`，标签与辅助 `11px`，按钮与正文 `13px`，统计数字 `22px`／`25px` |
| 间距 | 面板内边距 `20px`／`22px`，筛选栏 `20px`，表格工具条 `14px 20px`，分页 `16px 20px`；flex 间距常用 `8px`／`10px`／`12px` |
| 阴影 | 弹窗 `0 18px 60px #142b4322`，浮层提示 `0 4px 20px #233a551a`，输入聚焦 `0 0 0 2px #1677ff15` |
| 布局 | 侧栏宽 `226px`，顶栏高 `64px`，品牌区高 `76px` |

**禁止在页面里写死颜色、自造圆角与间距。** `.vue` 文件里出现十六进制色值会被 `npm run check` 判失败；需要新令牌时走第 8 章的公共改动流程。

样式文件分四份，均由原型移植，勿手工调数值：`tokens.css`（令牌）、`base.css`（元素默认样式）、`layout.css`（侧栏顶栏外壳）、`components.css`（共享组件样式）。原型里页面专有的类族（`trend-*`、`device-*`、`home-*`、`login-*` 等）暂未移植，随各自页面复刻时搬进该页的 `<style scoped>`。

## 3. 组件清单与契约

不引入第三方组件库。原型自带完整一套，逐个封装为 Vue 组件，放 `tms-web/src/components/ui/`。左列是原型里的类名或辅助函数，是复刻时的比对基准。

| 原型 | Vue 组件 | 契约要点 |
|---|---|---|
| `btn(label, code, cls)`，`.btn` / `.primary` / `.small` / `.danger` | `AppButton.vue` | `variant: default \| primary \| small \| danger`；原型用 `uiActionAllowed(code)` 控制显隐，Vue 里改用 `v-perm="'权限码'"` |
| `link(label, code, danger)`，`.link-btn` | `AppLinkButton.vue` | 表格行内操作用它，不用 `AppButton` |
| `.panel` / `.panel-pad` | `AppPanel.vue` | 白底、`1px var(--line)` 边框、圆角 8px、`overflow:hidden` |
| `heading(title, desc, buttons)` | `PageHeading.vue` | `title`、`description`、`actions` 插槽 |
| `metrics(items)`，`.stat-card` | `MetricCards.vue` | 每项 `{label, value, unit}`；支持 `accent-blue/green/cyan/purple/red/slate` 顶边色 |
| `filterBar(rows, config, placeholder)`，`.filterbar` / `.filter-field` | `FilterBar.vue` | 用原生 `<select>`、`<input type="date">`；筛选项按枚举完整取值渲染，不按当前页数据推导（详细设计 6.1） |
| `tableRows(rows, cols, actionFn)`，`.table-wrap` / `.table-top` | `DataTable.vue` | `table-top` 左侧显示「N 条记录」，右侧放导出；列定义 `[{key, label, width?}]` |
| `pagination(total)`，`.pagination` | `AppPagination.vue` | 左侧总数，右侧页码按钮 |
| `layer(title, body, footer, cls)`，`.dialog` / `.dialog-head/body/foot` | `AppDialog.vue` | 默认宽 760px，`wide` 970px，`narrow` 变体；`max-height: calc(100vh - 64px)` |
| `.drawer` / `.drawer-overlay` | `AppDrawer.vue` | 右侧滑出，遮罩右对齐 |
| `toast(msg)`，`.toast` | `useToast.ts` | 顶部居中浮层，默认带 ✓ 前缀；失败态用 `danger` 变体 |
| `badge(v)`，`.badge` + `green/red/blue/orange/purple` | `StatusBadge.vue` | **见下方注意事项** |
| `.chip` / `.picked-chips` | `AppChip.vue` | 多值展示，如角色、关联型号 |
| `.segmented` | `SegmentedControl.vue` | 工作台时间范围等切换 |
| `.notice` / `.notice.warn` | `AppNotice.vue` | 页面顶部说明条 |
| `.empty` / `.state-empty` | `AppEmpty.vue` | 空态，含图标与文字 |
| `.form-grid` + `inputField(name, label, value, type, required, full, help)` | `AppForm.vue` + `AppField.vue` | 原型用原生 `reportValidity()` 校验；Vue 侧包一层轻量校验（必填、长度、邮箱），错误文案走 i18n |
| `.tree` / `.tree-select` / `.tree-select-menu` | `OrgTreeSelect.vue` | 重写现有组件，去掉 `el-tree-select` |
| `.picker-*` | `DevicePicker.vue` | 设备选择器，随详细设计 3.9 展开时再做 |

**`StatusBadge` 的注意事项**：原型的 `badge()` 是用正则匹配**中文文案**决定颜色（`/成功|已激活|启用/` → green）。国际化之后英文文案匹配不上，这个做法不能照搬。Vue 侧按**枚举编码**映射颜色，取值以详细设计第 6 章为唯一来源，显示文案走 i18n 的 `enums.*` 键。这属于第 6 章登记的设计修正。

## 4. 页面清单

25 个菜单页加 3 个非菜单界面。菜单键与详细设计 6.2.2 权限目录逐一对应。

**列、列序、筛选项、按钮与说明文字不要去原型里自己读**——17 个配置驱动页面的这些内容已由 `npm run contracts` 从原型抽成 `tms-web/src/contracts/pages.ts`，页面实现一律从契约取。各自读原型再转述，正是多人协作时页面跑偏的主要原因。「原型入口」列只用于查看该页的整体结构与交互。

8 个自定义渲染的页面（见契约文件的 `BESPOKE_PAGES`）原型里没有 `configs` 条目，契约随各自页面复刻时补入同一个文件。

**复刻状态**取值：`待办` / `进行中（认领人）` / `已完成`。认领即改这一列，这张表是并行开发的唯一调度依据。

### 4.1 工作台

| 菜单键 | 页面 | 原型入口 | 后端接口 | 复刻状态 |
|---|---|---|---|---|
| `home` | 工作台 | `renderHome()` | 未就绪（3.20 未设计） | 待办 |

### 4.2 设备管理

| 菜单键 | 页面 | 原型入口 | 后端接口 | 复刻状态 |
|---|---|---|---|---|
| `devices` | 设备列表 | `renderDevices()` | 未就绪（3.9） | 待办 |
| `groups` | 设备分组 | `renderGroups()` | 未就绪（3.11） | 待办 |
| `operations` | 流转记录 | `renderGeneric()` | 未就绪（3.10） | 待办 |
| `mode` | 设备模式任务 | `renderTasks()` | 未就绪（3.12） | 待办 |

### 4.3 远程维护

| 菜单键 | 页面 | 原型入口 | 后端接口 | 复刻状态 |
|---|---|---|---|---|
| `packages` | 升级包管理 | `renderPackages()` | 未就绪（3.13） | 待办 |
| `ota` | OTA 任务 | `renderTasks()` | 未就绪（3.14） | 待办 |
| `ota-records` | OTA 执行记录 | `renderExecRecords()` | 未就绪（3.14） | 待办 |
| `keys` | 密钥管理 | `renderGeneric()` | 未就绪（3.15） | 待办 |
| `rki` | RKI 任务 | `renderTasks()` | 未就绪（3.16） | 待办 |
| `rki-records` | RKI 执行记录 | `renderExecRecords()` | 未就绪（3.16） | 待办 |

### 4.4 激活与证书

| 菜单键 | 页面 | 原型入口 | 后端接口 | 复刻状态 |
|---|---|---|---|---|
| `grants` | 激活授权 | `renderGeneric()` | 未就绪（3.17） | 待办 |
| `codes` | 授权码记录 | `renderGeneric()` | 未就绪（3.17） | 待办 |
| `activations` | 设备激活记录 | `renderGeneric()` | 未就绪（3.18） | 待办 |
| `cas` | CA 管理 | `renderGeneric()` | 未就绪（3.19） | 待办 |
| `certs` | 设备证书 | `renderGeneric()` | 未就绪（3.19） | 待办 |
| `servercerts` | 平台服务证书 | `renderGeneric()` | 未就绪（3.19） | 待办 |

### 4.5 系统管理（接口已就绪，优先复刻）

| 菜单键 | 页面 | 原型入口 | 后端接口 | 复刻状态 |
|---|---|---|---|---|
| `customers` | 客户管理 | `renderGeneric()` | 就绪（3.3 / 5.5） | 待办 |
| `orgs` | 机构管理 | `renderGeneric()` | 就绪（3.4 / 5.6） | 待办 |
| `members` | 成员管理 | `renderGeneric()` | 就绪（3.5 / 5.7） | 待办 |
| `roles` | 角色管理 | `renderGeneric()` | 就绪（3.2 / 5.4） | 待办 |
| `products` | 产品与型号 | `renderGeneric()` | 就绪（3.8 / 5.5） | 待办 |
| `logs` | 操作日志 | `renderGeneric()` | 就绪（3.6 / 5.8） | 待办 |
| `logins` | 登录日志 | `renderGeneric()` | 就绪（3.7 / 5.8） | 待办 |
| `sessions` | 在线会话 | `renderSessions()` | 就绪（3.7 / 5.8） | 待办 |

### 4.6 非菜单界面

| 标识 | 页面 | 原型入口 | 后端接口 | 复刻状态 |
|---|---|---|---|---|
| `login` | 登录页 | `showLogin()`，`.login-wrap` | 就绪（3.1 / 5.3） | 待办 |
| `profile` | 个人中心 | `renderProfile()` | 就绪（3.1 / 5.3） | 待办 |
| `change-password` | 首次强制改密页 | 原型无独立页，见第 6 章第 7 条 | 就绪（3.1 / 5.3） | 待办 |
| `device-detail` | 设备详情 | `renderDevicePage()` | 未就绪（3.9） | 待办 |

## 5. 单页复刻流程

1. **取契约**：列、列序、筛选项、按钮与说明文字从 `src/contracts/pages.ts` 取；区块顺序、交互细节、空态文案看原型对应的渲染函数。自定义渲染的页面先把契约补进契约文件。
2. **对文档**：拿功能清单与详细设计核对业务规则——权限码、数据范围、枚举取值、必填与长度、接口字段。原型与文档不一致的，按第 1 章仲裁，并登记到第 6 章。
3. **写页面**：只用第 3 章的组件和第 2 章的令牌。页面自己的样式写在 SFC 的 `<style scoped>` 里，不得改公共组件。
4. **录文案**：所有可见文字进 i18n，中英两份同时提交，规则见第 7 章。
5. **比对验收**：跑 `npm run check`，再按第 9 章执行；把该页从 `scripts/check.config.json` 的 `legacy` 里删掉，并把第 4 章的复刻状态改为「已完成」。

## 6. 差异登记表

原型与文档、或与实际需求冲突的逐条记录。**新发现的差异一律追加到这里**，不要在代码里静默处理。

「确认」列为**已确认**的条目，裁决经用户明确同意，按 CLAUDE.md 文档权威优先级第 1 条，优先级高于原型与详细设计，不要再翻案；
标**文档裁定**的，是按第 1 章规则直接以文档为准，无需另行确认。

| # | 差异 | 裁决 | 依据 | 确认 |
|---|---|---|---|---|
| 1 | 原型 `PLATFORM_ONLY` 含 `grants`、`codes`，共 7 个平台专属菜单 | 以文档为准，平台专属只有 `mode`、`cas`、`servercerts`、`customers`、`products` 五个 | 详细设计 6.2.1 已记录该差异 | 文档裁定 |
| 2 | 原型对平台排除 RKI 三菜单（`keys`、`rki`、`rki-records`） | 以文档为准，内置平台管理员覆盖全部权限码 | 详细设计 3.2.5 第 14 条、7.3 | 文档裁定 |
| 3 | 原型 `badge()` 用中文正则决定状态颜色 | 改为按枚举编码映射，文案走 `enums.*` | 详细设计第 6 章；中文正则在英文界面失效 | **已确认**（2026-09-20） |
| 4 | 原型登录页 `.login-wrap` 是左右分栏（`.login-art` 品牌区 + `.login-box` 表单），无验证码 | 布局按原型改成分栏，**保留验证码行** | 详细设计 3.1.5 第 12 条 | **已确认**（2026-09-20） |
| 5 | 原型「忘记密码」在登录页内原地切换标题与表单，现有实现是弹窗 | 按原型改成原地切换，验证码行同样保留 | 原型 `showLogin(forgot)` | **已确认**（2026-09-20） |
| 6 | 原型全部中文硬编码 | 复刻时一律进 i18n，中英双份 | 详细设计 7.13 | 文档裁定 |
| 7 | 原型「修改密码」是弹窗（`changeMyPassword()`），详细设计把「首次改密页」列为独立页面 | 两者都要：个人中心里的主动改密按原型走弹窗；首次登录与管理员重置后的强制改密仍是独立页面，因为此时要拦截其余所有页面 | 详细设计 3.1.2、3.1.5 第 9 条 | **已确认**（2026-09-20） |
| 8 | 原型用 `me().readOnly`、`view==='platform'` 这类演示态控制按钮显隐 | 换成 `v-perm` + 真实权限码；平台／客户差异走 `platform_only`，代码中不得出现硬编码菜单名单 | CLAUDE.md 权限红线 | 文档裁定 |
| 9 | 原型顶栏有全局设备 SN 搜索，现有实现没有 | 按原型补上，随 `devices` 页（3.9）一起做 | 原型 `globalSearch()` | **已确认**（2026-09-20） |

## 7. 国际化

- 所有可见文字走 i18n，`tms-web/src/i18n/locales/zh-CN.ts` 与 `en-US.ts` **必须同时改，键一一对应**。
- 公共文案进 `common`，枚举进 `enums`，菜单名进 `menu`／`menuGroup`。
- **页面文案进 `page.<菜单键驼峰>` 段**，键名由契约给出（如 `page.members.col.account`、`page.members.filter.status`、`page.members.create`、`page.members.description`），不要自己取名。这是为了让契约与语言包能机器对齐：`npm run check` 会校验每个已复刻页面的契约键中英文都存在。
- 现有的 `login`、`org`、`member`、`role`、`customer`、`product`、`operLog`、`loginLog`、`session`、`profile`、`home` 是复刻前的旧分段，随各自页面复刻迁入 `page.*` 后删除。
- 枚举展示名一律复用 `enums.*`，不在页面里另造文案。
- 中英文标点不同的组合文案用带占位符的键，如已有的 `common.labelValue`、`common.parenthesized`，不要在模板里拼接标点。
- 后端返回的 `xxxLabel` 字段已由服务端按 `Accept-Language` 解析，前端直接显示，不再二次翻译。

## 8. 多人／多 AI 协作约束

1. **一次认领一个页面**，认领时把第 4 章对应行的复刻状态改成「进行中（认领人）」，完成后改「已完成」。
2. **公共组件与设计令牌的改动必须单独提出**，不允许在做某个页面时顺手改 `components/ui/` 或 `styles/`。确实需要改的，先说明哪些页面受影响。
3. 页面之间不共享临时样式类；页面特有样式写在自己的 `<style scoped>` 里。
4. 新增差异一律追加到第 6 章，不在代码注释里写「原型这里不合理」。
5. 提交遵守 CLAUDE.md：一次提交只做一件事，格式 `<type>: <简要说明>`。按页复刻时，一页一提交。

### 8.1 靠什么保证真的统一

文档只能提醒，约束要靠这三样：

| 层 | 机制 | 位置 |
|---|---|---|
| 唯一数据源 | 列与筛选项只有契约、颜色只有令牌、文案只有语言包、枚举只有 `stores/dict.ts`、权限码只有后端权限目录 | `src/contracts/pages.ts`、`src/styles/tokens.css` |
| 自动校验 | `npm run check` 六项：类型检查、中英文键对齐、用到的键都存在、`.vue` 里无硬编码色值、`.vue` 里无硬编码中文、已复刻页面必须从契约取且不得引用 element-plus | `scripts/check.mjs` |
| 持续集成 | push 与 PR 自动跑前后端全部校验，并校验契约与原型是否同步 | `.github/workflows/ci.yml` |

`scripts/check.config.json` 的 `legacy` 列出尚未复刻的文件，暂时豁免严格规则。**复刻完一个页面就把它从 `legacy` 删掉**——删不掉说明没真正迁完。`pages` 段登记页面文件与契约键的对应，新增页面时补上。

契约文件由 `npm run contracts` 从原型生成，**不要手工编辑**；原型改了就重跑，CI 会校验两者是否一致。

## 9. 验收标准

0. **`npm run check` 通过**：这是硬门槛，跑不过不提交。
1. **视觉比对**：同一页面按原型相同视口截图，与原型并排看，区块顺序、列与列序、筛选项、按钮位置与文案无明显差异。
2. **双语**：中英文各走一遍，无硬编码中文，无键缺失，英文下不出现文字截断。
3. **权限**：按权限码控制的操作入口，用无权账号验证确实不可见；按 ID 操作的记录要验证数据范围校验生效。
4. **枚举**：筛选项按枚举完整取值渲染，不随当前页数据变化。
5. **构建**：`npm run build`（含 `vue-tsc`）通过。

提交时把 `npm run check` 的输出贴出来，方便他人复核。

## 10. 落地收尾

全部页面复刻完成后一次性处理，不要边做边改：

1. 从 `tms-web/package.json` 移除 `element-plus` 与 `@element-plus/icons-vue`，删掉 `main.ts` 与 `App.vue` 里的相关注册和 `el-config-provider`。
2. 同步修订：详细设计 2.3 技术选型的前端那行、2.5.6 前端目录、7.13 中「同时切换 Element Plus 的组件语言包」一句。
3. 同步修订 `CLAUDE.md` 技术栈表的前端那行。
4. 复核打包体积，确认主包显著下降。
