package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

import java.util.List;

@Data
public class PermissionCatalogVO {

    private String groupName;
    private String menuKey;
    private String menuName;
    private List<ActionVO> actions;

    @Data
    public static class ActionVO {
        private String permCode;
        private String action;
        private String actionName;
        /** 操作者是否有权授予该权限项，不可授予时页面置灰但仍展示 */
        private Boolean grantable;
    }
}
