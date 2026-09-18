# 数据库脚本

| 文件 | 说明 |
| --- | --- |
| `schema.sql` | 基座表结构，对应 `docs/TMS-详细设计.md` 4.3 的 15 张表 |
| `init-data.sql` | 平台租户（`t_tenant.id = 0`）与平台根机构（`t_org.id = 1`） |

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

设备、任务、升级包、密钥、激活、证书等表随对应模块的详细设计补充，本目录暂不包含。
