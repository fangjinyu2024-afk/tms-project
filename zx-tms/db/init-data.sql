-- TMS 初始化数据：仅平台租户与平台根机构。
-- 权限目录、平台功能授权、内置平台管理员角色与首个平台账号由 tms-admin 启动时同步创建（SystemInitializer）。

USE `tms`;

INSERT INTO `t_org` (`id`, `tenant_id`, `parent_id`, `org_path`, `org_type`, `name`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (1, 0, 0, '/1/', 'PLATFORM', '平台', 'ENABLED', 0, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `org_path` = VALUES(`org_path`);

INSERT INTO `t_tenant` (`id`, `name`, `root_org_id`, `status`, `auth_code_channel`, `feature_version`, `deleted`, `create_time`, `update_time`)
VALUES (0, '平台', 1, 'ENABLED', 'TOOL', 1, 0, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `root_org_id` = VALUES(`root_org_id`);
