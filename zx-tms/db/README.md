# 数据库脚本

| 文件 | 说明 |
| --- | --- |
| `schema.sql` | 基座表结构，对应 `docs/TMS-详细设计.md` 4.3 的 15 张表 |
| `init-data.sql` | 平台租户（`t_tenant.id = 0`）与平台根机构（`t_org.id = 1`） |
| `migration/` | 增量脚本，供已建库实例升级；新库执行 `schema.sql` 即可 |

## 执行顺序

```bash
mysql -u root -p < schema.sql
mysql -u root -p < init-data.sql
```

随后启动 `tms-admin`，`SystemInitializer` 会完成：

1. 权限目录同步：按权限码 upsert 到 `t_permission`，目录中已移除的权限码置为停用；
2. 平台功能授权：`t_tenant_feature` 补齐平台租户的全部菜单；
3. 内置平台管理员角色：按权限目录同步权限码（不含 `keys` / `rki` / `rki-records`）；
4. 首个平台账号：账号取 `tms.init.admin-account`（默认 `admin`），初始密码取 `tms.init.admin-password`（默认 `Tms@12345`），`must_change_password = 1`，首次登录必须修改。

生产部署请在启动前通过环境变量覆盖初始密码，例如 `TMS_INIT_ADMIN_PASSWORD`；初始密码只用于首次登录，不会写入日志。

## 逻辑删除与唯一索引

主数据表的 `deleted` 仍是 0／1 标记，业务唯一索引使用生成列 `delete_key`（未删除为 0，已删除为主键）。
这样「同名记录删除后重建、再次删除」不会撞唯一键，同时未删除记录的业务唯一性依旧由数据库兜底。
已按早期脚本建库的实例执行 `migration/V1.1__logic_delete_unique_key.sql` 升级。

设备、任务、升级包、密钥、激活、证书等表随对应模块的详细设计补充，本目录暂不包含。
