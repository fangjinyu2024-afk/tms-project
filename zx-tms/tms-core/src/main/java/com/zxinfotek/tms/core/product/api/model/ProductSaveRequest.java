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

    @NotNull(message = "msg.valid.productCategoryRequired")
    private ProductCategory category;

    @NotBlank(message = "msg.valid.productNameRequired")
    @Size(max = 100, message = "msg.valid.productNameSize")
    private String name;

    @Size(max = 255, message = "msg.valid.imagePathSize")
    private String imagePath;

    @Size(max = 500, message = "msg.valid.productDescriptionSize")
    private String description;

    @NotEmpty(message = "msg.valid.modelsRequired")
    private List<ModelItem> models;

    @Data
    public static class ModelItem {
        /** 已有型号的主键，新增型号留空 */
        private Long id;

        @NotBlank(message = "msg.valid.modelRequired")
        @Size(max = 50, message = "msg.valid.modelSize")
        private String model;
    }
}
