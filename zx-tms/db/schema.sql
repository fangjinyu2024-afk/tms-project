-- TMS 终端管理系统 数据库初始化脚本（基座部分）
-- 表结构与索引以 docs/TMS-详细设计.md 第 4 章为准；设备、任务、升级包、密钥、激活、证书等表随对应模块补充。
-- 约定：InnoDB / utf8mb4；主键雪花算法由应用生成；时间字段 DATETIME(3) 统一存 UTC；不建物理外键。

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS `tms` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `tms`;

-- 1. 客户（租户）表
CREATE TABLE IF NOT EXISTS `t_tenant` (
  `id`                BIGINT UNSIGNED NOT NULL COMMENT '主键，平台固定为 0',
  `name`              VARCHAR(100)    NOT NULL COMMENT '客户名称',
  `root_org_id`       BIGINT UNSIGNED NOT NULL COMMENT '客户根机构 ID',
  `contact_name`      VARCHAR(50)     DEFAULT NULL COMMENT '联系人',
  `contact_phone`     VARCHAR(30)     DEFAULT NULL COMMENT '联系电话',
  `country`           VARCHAR(50)     DEFAULT NULL COMMENT '国家／地区',
  `province`          VARCHAR(50)     DEFAULT NULL COMMENT '省／州',
  `city`              VARCHAR(50)     DEFAULT NULL COMMENT '城市',
  `remark`            VARCHAR(500)    DEFAULT NULL COMMENT '备注',
  `status`            VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '状态，取值见详细设计 6.2.6',
  `auth_code_channel` VARCHAR(10)     NOT NULL DEFAULT 'TOOL' COMMENT '授权码发放渠道，取值见 6.3.9',
  `feature_version`   INT             NOT NULL DEFAULT 1 COMMENT '功能授权版本，变更时自增',
  `deleted`           TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by`         BIGINT UNSIGNED DEFAULT NULL,
  `create_by_name`    VARCHAR(50)     DEFAULT NULL,
  `create_time`       DATETIME(3)     DEFAULT NULL,
  `update_by`         BIGINT UNSIGNED DEFAULT NULL,
  `update_by_name`    VARCHAR(50)     DEFAULT NULL,
  `update_time`       DATETIME(3)     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户（租户）表';

-- 2. 客户功能授权表
CREATE TABLE IF NOT EXISTS `t_tenant_feature` (
  `id`          BIGINT UNSIGNED NOT NULL COMMENT '主键',
  `tenant_id`   BIGINT UNSIGNED NOT NULL COMMENT '客户 ID',
  `menu_key`    VARCHAR(40)     NOT NULL COMMENT '菜单键，取值见详细设计 6.2.2',
  `create_time` DATETIME(3)     NOT NULL COMMENT '开通时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_menu` (`tenant_id`, `menu_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户功能授权表';

-- 3. 机构表
CREATE TABLE IF NOT EXISTS `t_org` (
  `id`             BIGINT UNSIGNED NOT NULL COMMENT '主键',
  `tenant_id`      BIGINT UNSIGNED NOT NULL COMMENT '所属客户，平台为 0',
  `parent_id`      BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '上级机构，根机构为 0',
  `org_path`       VARCHAR(255)    NOT NULL COMMENT '物化路径 /根ID/.../本级ID/',
  `org_type`       VARCHAR(20)     NOT NULL COMMENT '机构类型，取值见详细设计 6.2.3',
  `name`           VARCHAR(100)    NOT NULL COMMENT '机构名称，同一上级下唯一',
  `contact_name`   VARCHAR(50)     DEFAULT NULL COMMENT '联系人',
  `contact_phone`  VARCHAR(30)     DEFAULT NULL COMMENT '联系方式',
  `status`         VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '状态，取值见详细设计 6.2.6',
  `deleted`        TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by`      BIGINT UNSIGNED DEFAULT NULL,
  `create_by_name` VARCHAR(50)     DEFAULT NULL,
  `create_time`    DATETIME(3)     DEFAULT NULL,
  `update_by`      BIGINT UNSIGNED DEFAULT NULL,
  `update_by_name` VARCHAR(50)     DEFAULT NULL,
  `update_time`    DATETIME(3)     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_parent_name` (`parent_id`, `name`, `deleted`),
  KEY `idx_tenant_path` (`tenant_id`, `org_path`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机构表';

-- 4. 成员表
CREATE TABLE IF NOT EXISTS `t_member` (
  `id`                    BIGINT UNSIGNED NOT NULL COMMENT '主键，即成员编号',
  `tenant_id`             BIGINT UNSIGNED NOT NULL COMMENT '所属客户',
  `org_id`                BIGINT UNSIGNED NOT NULL COMMENT '所属机构',
  `org_path`              VARCHAR(255)    NOT NULL COMMENT '所属机构路径',
  `account`               VARCHAR(64)     NOT NULL COMMENT '登录账号，原样保存',
  `account_lower`         VARCHAR(64)     NOT NULL COMMENT '账号小写，用于唯一判重',
  `nickname`              VARCHAR(50)     NOT NULL COMMENT '昵称／姓名',
  `password_hash`         VARCHAR(100)    NOT NULL COMMENT 'BCrypt 哈希，不存明文',
  `email`                 VARCHAR(128)    DEFAULT NULL COMMENT '邮箱',
  `email_verified`        TINYINT         NOT NULL DEFAULT 0 COMMENT '邮箱是否已验证',
  `phone`                 VARCHAR(30)     DEFAULT NULL COMMENT '联系电话',
  `must_change_password`  TINYINT         NOT NULL DEFAULT 1 COMMENT '是否需要强制改密',
  `password_updated_at`   DATETIME(3)     DEFAULT NULL COMMENT '最近改密时间',
  `fail_count`            INT             NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  `locked_until`          DATETIME(3)     DEFAULT NULL COMMENT '锁定截止时间',
  `perm_version`          INT             NOT NULL DEFAULT 1 COMMENT '成员权限版本，角色变更时自增',
  `status`                VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '状态，取值见详细设计 6.2.6',
  `deleted`               TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by`             BIGINT UNSIGNED DEFAULT NULL,
  `create_by_name`        VARCHAR(50)     DEFAULT NULL,
  `create_time`           DATETIME(3)     DEFAULT NULL,
  `update_by`             BIGINT UNSIGNED DEFAULT NULL,
  `update_by_name`        VARCHAR(50)     DEFAULT NULL,
  `update_time`           DATETIME(3)     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account_lower`, `deleted`),
  UNIQUE KEY `uk_email` (`email`, `deleted`),
  KEY `idx_tenant_org` (`tenant_id`, `org_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成员表';

-- 5. 角色表
CREATE TABLE IF NOT EXISTS `t_role` (
  `id`             BIGINT UNSIGNED NOT NULL COMMENT '主键',
  `tenant_id`      BIGINT UNSIGNED NOT NULL COMMENT '所属客户',
  `owner_org_id`   BIGINT UNSIGNED NOT NULL COMMENT '归属机构，只有该机构可编辑',
  `owner_org_path` VARCHAR(255)    NOT NULL COMMENT '归属机构路径',
  `name`           VARCHAR(50)     NOT NULL COMMENT '角色名称，同一机构内唯一',
  `description`    VARCHAR(200)    DEFAULT NULL COMMENT '说明',
  `data_scope`     VARCHAR(20)     NOT NULL DEFAULT 'ORG_AND_SUB' COMMENT '可管理范围，取值见 6.2.10',
  `builtin_code`   VARCHAR(40)     DEFAULT NULL COMMENT '内置角色编码，非空表示内置角色',
  `perm_version`   INT             NOT NULL DEFAULT 1 COMMENT '角色权限版本',
  `status`         VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '状态，取值见详细设计 6.2.6',
  `version`        INT             NOT NULL DEFAULT 0 COMMENT '乐观锁',
  `deleted`        TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by`      BIGINT UNSIGNED DEFAULT NULL,
  `create_by_name` VARCHAR(50)     DEFAULT NULL,
  `create_time`    DATETIME(3)     DEFAULT NULL,
  `update_by`      BIGINT UNSIGNED DEFAULT NULL,
  `update_by_name` VARCHAR(50)     DEFAULT NULL,
  `update_time`    DATETIME(3)     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_name` (`owner_org_id`, `name`, `deleted`),
  KEY `idx_tenant_path` (`tenant_id`, `owner_org_path`),
  KEY `idx_builtin` (`tenant_id`, `builtin_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- 6. 角色权限表
CREATE TABLE IF NOT EXISTS `t_role_perm` (
  `id`        BIGINT UNSIGNED NOT NULL COMMENT '主键',
  `role_id`   BIGINT UNSIGNED NOT NULL COMMENT '角色 ID',
  `perm_code` VARCHAR(60)     NOT NULL COMMENT '权限码 菜单键:操作',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `perm_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限表';

-- 7. 成员角色关系表
CREATE TABLE IF NOT EXISTS `t_member_role` (
  `id`        BIGINT UNSIGNED NOT NULL COMMENT '主键',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '成员 ID',
  `role_id`   BIGINT UNSIGNED NOT NULL COMMENT '角色 ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_role` (`member_id`, `role_id`),
  KEY `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成员角色关系表';

-- 8. 权限目录表（服务启动时由代码常量同步）
CREATE TABLE IF NOT EXISTS `t_permission` (
  `id`            BIGINT UNSIGNED NOT NULL COMMENT '主键',
  `perm_code`     VARCHAR(60)     NOT NULL COMMENT '权限码，全局唯一',
  `menu_key`      VARCHAR(40)     NOT NULL COMMENT '菜单键',
  `menu_name`     VARCHAR(50)     NOT NULL COMMENT '菜单名称',
  `group_name`    VARCHAR(50)     NOT NULL COMMENT '菜单分组',
  `action`        VARCHAR(30)     NOT NULL COMMENT '操作',
  `action_name`   VARCHAR(30)     NOT NULL COMMENT '操作名称',
  `platform_only` TINYINT         NOT NULL DEFAULT 0 COMMENT '是否仅平台账户可用，权限码级',
  `sort`          INT             NOT NULL DEFAULT 0 COMMENT '排序',
  `status`        VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '目录中已移除的权限置为停用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`),
  KEY `idx_menu` (`menu_key`, `sort`),
  KEY `idx_platform_only` (`platform_only`, `menu_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限目录表';

-- 9. 登录会话表
CREATE TABLE IF NOT EXISTS `t_login_session` (
  `id`               BIGINT UNSIGNED NOT NULL COMMENT '主键，即会话编号',
  `tenant_id`        BIGINT UNSIGNED NOT NULL COMMENT '所属客户',
  `org_id`           BIGINT UNSIGNED NOT NULL COMMENT '所属机构',
  `org_path`         VARCHAR(255)    NOT NULL COMMENT '机构路径',
  `member_id`        BIGINT UNSIGNED NOT NULL COMMENT '成员 ID',
  `account`          VARCHAR(64)     NOT NULL COMMENT '账号快照',
  `nickname`         VARCHAR(50)     NOT NULL COMMENT '昵称快照',
  `token_hash`       CHAR(64)        NOT NULL COMMENT '令牌 SHA-256 十六进制，不存明文',
  `entry`            VARCHAR(20)     NOT NULL COMMENT '会话入口，取值见 6.2.11',
  `client_ip`        VARCHAR(45)     NOT NULL COMMENT '登录 IP',
  `user_agent`       VARCHAR(255)    DEFAULT NULL COMMENT '浏览器或客户端标识',
  `login_time`       DATETIME(3)     NOT NULL COMMENT '登录时间',
  `last_active_time` DATETIME(3)     NOT NULL COMMENT '最近活动时间',
  `expire_time`      DATETIME(3)     NOT NULL COMMENT '绝对过期时间',
  `status`           VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '会话状态，取值见 6.2.4',
  `invalid_reason`   VARCHAR(30)     DEFAULT NULL COMMENT '失效原因，取值见 6.2.5',
  `invalid_time`     DATETIME(3)     DEFAULT NULL COMMENT '失效时间',
  `invalid_by`       BIGINT UNSIGNED DEFAULT NULL COMMENT '强制下线操作人',
  `invalid_remark`   VARCHAR(200)    DEFAULT NULL COMMENT '强制下线原因说明',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token_hash` (`token_hash`),
  KEY `idx_member_status` (`member_id`, `status`),
  KEY `idx_scope_status` (`tenant_id`, `org_path`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录会话表';

-- 10. 登录日志表
CREATE TABLE IF NOT EXISTS `t_login_log` (
  `id`          BIGINT UNSIGNED NOT NULL COMMENT '主键',
  `tenant_id`   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '所属客户，账号不存在时为 0',
  `org_id`      BIGINT UNSIGNED DEFAULT NULL COMMENT '所属机构',
  `org_path`    VARCHAR(255)    DEFAULT NULL COMMENT '机构路径',
  `member_id`   BIGINT UNSIGNED DEFAULT NULL COMMENT '成员 ID',
  `account`     VARCHAR(64)     NOT NULL COMMENT '提交的账号',
  `entry`       VARCHAR(20)     NOT NULL COMMENT '登录入口，取值见 6.2.11',
  `result`      VARCHAR(20)     NOT NULL COMMENT '登录结果，取值见 6.2.12',
  `fail_reason` VARCHAR(100)    DEFAULT NULL COMMENT '失败原因',
  `client_ip`   VARCHAR(45)     NOT NULL COMMENT '来源 IP',
  `user_agent`  VARCHAR(255)    DEFAULT NULL COMMENT '浏览器／系统',
  `trace_id`    CHAR(32)        NOT NULL COMMENT '关联编号',
  `login_time`  DATETIME(3)     NOT NULL COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_account_time` (`account`, `login_time`),
  KEY `idx_scope_time` (`tenant_id`, `org_path`, `login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

-- 11. 操作日志表
CREATE TABLE IF NOT EXISTS `t_oper_log` (
  `id`             BIGINT UNSIGNED NOT NULL COMMENT '主键',
  `tenant_id`      BIGINT UNSIGNED NOT NULL COMMENT '操作者所属客户',
  `org_id`         BIGINT UNSIGNED DEFAULT NULL COMMENT '操作者所属机构',
  `org_path`       VARCHAR(255)    DEFAULT NULL COMMENT '机构路径',
  `member_id`      BIGINT UNSIGNED DEFAULT NULL COMMENT '操作者',
  `account`        VARCHAR(64)     DEFAULT NULL COMMENT '账号快照',
  `nickname`       VARCHAR(50)     DEFAULT NULL COMMENT '昵称快照',
  `module`         VARCHAR(40)     NOT NULL COMMENT '业务模块编码，取值见 6.2.7',
  `action`         VARCHAR(30)     NOT NULL COMMENT '操作类型，取值见 6.2.8',
  `object_type`    VARCHAR(40)     DEFAULT NULL COMMENT '业务对象类型',
  `object_id`      VARCHAR(64)     DEFAULT NULL COMMENT '业务对象编号',
  `object_name`    VARCHAR(200)    DEFAULT NULL COMMENT '操作时的对象名称',
  `result`         VARCHAR(20)     NOT NULL COMMENT '操作结果，取值见 6.2.13',
  `fail_reason`    VARCHAR(500)    DEFAULT NULL COMMENT '失败原因',
  `total_count`    INT             DEFAULT NULL COMMENT '批量业务总数',
  `success_count`  INT             DEFAULT NULL COMMENT '批量业务成功数',
  `change_summary` JSON            DEFAULT NULL COMMENT '字段白名单内的变更前后摘要',
  `client_ip`      VARCHAR(45)     DEFAULT NULL COMMENT '来源 IP',
  `trace_id`       CHAR(32)        NOT NULL COMMENT '关联编号',
  `oper_time`      DATETIME(3)     NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trace_module_action` (`trace_id`, `module`, `action`),
  KEY `idx_scope_time` (`tenant_id`, `org_path`, `oper_time`),
  KEY `idx_module_action_time` (`module`, `action`, `oper_time`),
  KEY `idx_object` (`object_type`, `object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

-- 12. 接口访问日志表（保留 30 天，由定时任务清理）
CREATE TABLE IF NOT EXISTS `t_api_access_log` (
  `id`          BIGINT UNSIGNED NOT NULL COMMENT '主键',
  `trace_id`    CHAR(32)        NOT NULL COMMENT '关联编号',
  `member_id`   BIGINT UNSIGNED DEFAULT NULL COMMENT '调用者，未登录为空',
  `method`      VARCHAR(10)     NOT NULL COMMENT 'HTTP 方法',
  `path`        VARCHAR(255)    NOT NULL COMMENT '请求路径',
  `http_status` INT             NOT NULL COMMENT 'HTTP 状态码',
  `biz_code`    VARCHAR(20)     DEFAULT NULL COMMENT '业务错误码',
  `duration_ms` INT             NOT NULL COMMENT '耗时（毫秒）',
  `client_ip`   VARCHAR(45)     NOT NULL COMMENT '来源 IP',
  `create_time` DATETIME(3)     NOT NULL COMMENT '请求时间',
  PRIMARY KEY (`id`),
  KEY `idx_trace` (`trace_id`),
  KEY `idx_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='接口访问日志表';

-- 13. 产品表
CREATE TABLE IF NOT EXISTS `t_product` (
  `id`             BIGINT UNSIGNED NOT NULL COMMENT '主键',
  `category`       VARCHAR(20)     NOT NULL COMMENT '产品类别，取值见 6.2.9',
  `name`           VARCHAR(100)    NOT NULL COMMENT '产品名称',
  `image_path`     VARCHAR(255)    DEFAULT NULL COMMENT '产品图片对象存储路径',
  `description`    VARCHAR(500)    DEFAULT NULL COMMENT '描述',
  `deleted`        TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by`      BIGINT UNSIGNED DEFAULT NULL,
  `create_by_name` VARCHAR(50)     DEFAULT NULL,
  `create_time`    DATETIME(3)     DEFAULT NULL,
  `update_by`      BIGINT UNSIGNED DEFAULT NULL,
  `update_by_name` VARCHAR(50)     DEFAULT NULL,
  `update_time`    DATETIME(3)     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品表';

-- 14. 产品型号表
CREATE TABLE IF NOT EXISTS `t_product_model` (
  `id`             BIGINT UNSIGNED NOT NULL COMMENT '主键',
  `product_id`     BIGINT UNSIGNED NOT NULL COMMENT '所属产品',
  `model`          VARCHAR(50)     NOT NULL COMMENT '型号标识，平台内唯一',
  `deleted`        TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by`      BIGINT UNSIGNED DEFAULT NULL,
  `create_by_name` VARCHAR(50)     DEFAULT NULL,
  `create_time`    DATETIME(3)     DEFAULT NULL,
  `update_by`      BIGINT UNSIGNED DEFAULT NULL,
  `update_by_name` VARCHAR(50)     DEFAULT NULL,
  `update_time`    DATETIME(3)     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_model` (`model`, `deleted`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品型号表';

-- 15. 客户型号授权表
CREATE TABLE IF NOT EXISTS `t_tenant_model` (
  `id`          BIGINT UNSIGNED NOT NULL COMMENT '主键',
  `tenant_id`   BIGINT UNSIGNED NOT NULL COMMENT '客户 ID',
  `model_id`    BIGINT UNSIGNED NOT NULL COMMENT '型号 ID',
  `create_time` DATETIME(3)     NOT NULL COMMENT '授权时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_model` (`tenant_id`, `model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户型号授权表';
