package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

import java.util.List;

@Data
public class TenantFeatureVO {

    private Long tenantId;
    private List<String> menuKeys;
    private List<String> added;
    private List<String> removed;
    private List<MenuOptionVO> options;

    @Data
    public static class MenuOptionVO {
        private String menuKey;
        private String menuName;
        private String groupName;
        /** 工作台强制包含且不可取消 */
        private Boolean required;
    }
}
