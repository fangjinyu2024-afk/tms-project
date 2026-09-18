package com.zxinfotek.tms.core.product.api.model;

import com.zxinfotek.tms.common.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProductSaveRequest {

    @NotNull(message = "请选择产品类别")
    private ProductCategory category;

    @NotBlank(message = "请输入产品名称")
    @Size(max = 100, message = "产品名称最多 100 个字符")
    private String name;

    @Size(max = 255)
    private String imagePath;

    @Size(max = 500)
    private String description;

    @NotEmpty(message = "请至少添加一个型号")
    private List<ModelItem> models;

    @Data
    public static class ModelItem {
        /** 已有型号的主键，新增型号留空 */
        private Long id;

        @NotBlank(message = "请输入型号标识")
        @Size(max = 50)
        private String model;
    }
}
