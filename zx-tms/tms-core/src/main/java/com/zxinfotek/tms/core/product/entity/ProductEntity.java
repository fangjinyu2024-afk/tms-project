package com.zxinfotek.tms.core.product.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zxinfotek.tms.common.entity.BaseEntity;
import com.zxinfotek.tms.common.enums.ProductCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product")
public class ProductEntity extends BaseEntity {

    private ProductCategory category;
    private String name;
    private String imagePath;
    private String description;

    @TableLogic
    private Integer deleted;
}
