-- 逻辑删除唯一索引优化：业务唯一索引改用生成列 delete_key
-- 原索引 (业务键, deleted) 只允许存在一条已删除记录，「删除同名记录两次」会撞唯一键；
-- delete_key 在未删除时为 0、已删除时为主键，使同名记录可以反复删除重建。
-- 适用于已按 V1.0 schema.sql 建库的实例；新库直接执行 schema.sql 即可。

USE `tms`;

ALTER TABLE `t_tenant` ADD COLUMN `delete_key` BIGINT UNSIGNED
  GENERATED ALWAYS AS (IF(`deleted` = 0, 0, `id`)) STORED
  COMMENT '逻辑删除唯一键：未删除为 0，已删除为主键，供业务唯一索引使用';
ALTER TABLE `t_tenant` DROP INDEX `uk_name`;
ALTER TABLE `t_tenant` ADD UNIQUE KEY `uk_name` (`name`, `delete_key`);

ALTER TABLE `t_org` ADD COLUMN `delete_key` BIGINT UNSIGNED
  GENERATED ALWAYS AS (IF(`deleted` = 0, 0, `id`)) STORED
  COMMENT '逻辑删除唯一键：未删除为 0，已删除为主键，供业务唯一索引使用';
ALTER TABLE `t_org` DROP INDEX `uk_parent_name`;
ALTER TABLE `t_org` ADD UNIQUE KEY `uk_parent_name` (`parent_id`, `name`, `delete_key`);

ALTER TABLE `t_member` ADD COLUMN `delete_key` BIGINT UNSIGNED
  GENERATED ALWAYS AS (IF(`deleted` = 0, 0, `id`)) STORED
  COMMENT '逻辑删除唯一键：未删除为 0，已删除为主键，供业务唯一索引使用';
ALTER TABLE `t_member` DROP INDEX `uk_account`;
ALTER TABLE `t_member` ADD UNIQUE KEY `uk_account` (`account_lower`, `delete_key`);
ALTER TABLE `t_member` DROP INDEX `uk_email`;
ALTER TABLE `t_member` ADD UNIQUE KEY `uk_email` (`email`, `delete_key`);

ALTER TABLE `t_role` ADD COLUMN `delete_key` BIGINT UNSIGNED
  GENERATED ALWAYS AS (IF(`deleted` = 0, 0, `id`)) STORED
  COMMENT '逻辑删除唯一键：未删除为 0，已删除为主键，供业务唯一索引使用';
ALTER TABLE `t_role` DROP INDEX `uk_org_name`;
ALTER TABLE `t_role` ADD UNIQUE KEY `uk_org_name` (`owner_org_id`, `name`, `delete_key`);

ALTER TABLE `t_product` ADD COLUMN `delete_key` BIGINT UNSIGNED
  GENERATED ALWAYS AS (IF(`deleted` = 0, 0, `id`)) STORED
  COMMENT '逻辑删除唯一键：未删除为 0，已删除为主键，供业务唯一索引使用';
ALTER TABLE `t_product` DROP INDEX `uk_name`;
ALTER TABLE `t_product` ADD UNIQUE KEY `uk_name` (`name`, `delete_key`);

ALTER TABLE `t_product_model` ADD COLUMN `delete_key` BIGINT UNSIGNED
  GENERATED ALWAYS AS (IF(`deleted` = 0, 0, `id`)) STORED
  COMMENT '逻辑删除唯一键：未删除为 0，已删除为主键，供业务唯一索引使用';
ALTER TABLE `t_product_model` DROP INDEX `uk_model`;
ALTER TABLE `t_product_model` ADD UNIQUE KEY `uk_model` (`model`, `delete_key`);
