# TMS 终端管理系统

POS 终端交付后的集中管理平台（设备激活与初始化、OTA 升级、RKI 远程密钥分发、设备模式控制，以及客户、机构、账户权限、产品型号、设备流转、证书与日志管理）。

当前交付范围：需求与页面设计资料，以及按详细设计实现的**基座模块**——认证与会话、权限与角色、客户与功能授权、机构、成员、操作日志、登录日志与在线会话、产品与型号（详细设计 3.1～3.8）。设备、OTA、RKI、激活、证书与工作台（3.9～3.20）随对应章节的详细设计补写，相关模块目前只有工程骨架。

## 目录

| 路径 | 说明 |
| --- | --- |
| `docs/TMS-详细设计.md` | 系统详细设计：项目说明、总体架构、工程结构与模块划分、基座模块详细设计、数据库、接口、状态枚举、关键技术处理、部署与待确认事项 |
| `docs/TMS-功能清单.md` | 系统功能清单：使用角色、各模块功能与字段、跨模块统一规则、本阶段边界与设计依据 |
| `docs/protocol/` | 设备与 TMS 的 TCP 报文协议：激活、密钥下载（RKI）、OTA 升级；含原始 Word 文档与转换后的 Markdown |
| `prototype/TMS-原型.html` | 低保真可交互原型，单文件 HTML，浏览器直接打开 |
| `zx-tms/` | 后端 Maven 聚合工程，10 个模块；`zx-tms/db/` 为数据库脚本 |
| `tms-web/` | 前端 Vue 3 + TypeScript + Vite + Element Plus 工程 |
| `CLAUDE.md` | 工程约定：文档权威优先级、技术栈、模块划分与跨模块红线、编码与数据库规范、权限与安全红线、协议实现约束、注释规范 |

原型与功能清单存在差异时，以功能清单中本轮确认的规则为准；详细设计与功能清单的关系见 `CLAUDE.md` 的「文档权威优先级」。

## 后端

```text
zx-tms/
├── tms-common      通用契约、Result、异常、错误码、分页、BaseEntity、雪花 ID、UTC 时间工具
├── tms-crypto      密码学能力（骨架，随激活／RKI／证书模块实现）
├── tms-infra       DB / Redis / MinIO / Mail / Lock / MyBatis 拦截器 / 请求上下文
├── tms-task        任务骨架（骨架，随任务类模块实现）
├── tms-core        iam / audit / product 已实现，device / cert 随对应章节补写
├── tms-activation  激活（骨架）
├── tms-ota         OTA（骨架）
├── tms-rki         RKI（骨架）
├── tms-admin       【可执行】管理后台进程
└── tms-gateway     设备 TCP 接入（骨架）
```

### 本地运行

前置：JDK 17、Maven 3.9、MySQL 8.0、Redis 7；导出功能另需 MinIO。

```bash
# 1. 建库与初始化数据（详见 zx-tms/db/README.md）
mysql -u root -p < zx-tms/db/schema.sql
mysql -u root -p < zx-tms/db/init-data.sql

# 2. 构建并启动管理后台（默认端口 8080）
cd zx-tms
mvn -DskipTests package
java -jar tms-admin/target/tms-admin-1.0.0-SNAPSHOT.jar
```

首次启动会同步权限目录（25 菜单、120 权限码）、初始化平台租户与内置平台管理员角色，并创建首个平台账号（默认 `admin`，初始密码取 `tms.init.admin-password`，首次登录强制修改）。数据库连接、Redis、对象存储等配置见 `zx-tms/tms-admin/src/main/resources/application.yml`。

接口文档：启动后访问 `/swagger-ui.html`。

### 测试

```bash
cd zx-tms
mvn test          # 含 ArchUnit 跨模块约束校验与权限目录口径校验
```

## 前端

```bash
cd tms-web
npm install
npm run dev       # 开发服务器 5173，/api 代理到 127.0.0.1:8080
npm run build     # 类型检查 + 产物构建
```

页面与后端接口一一对应：登录、首次改密、个人中心、工作台（统计随 3.20 实现）、客户管理与功能授权、机构管理、成员管理、角色管理与权限配置、产品与型号、操作日志、登录日志、在线会话。
