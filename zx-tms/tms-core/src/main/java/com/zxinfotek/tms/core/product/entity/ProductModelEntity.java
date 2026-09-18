package com.zxinfotek.tms.core.product.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zxinfotek.tms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product_model")
public class ProductModelEntity extends BaseEntity {

    private Long productId;
    private String model;

    @TableLogic
    private Integer deleted;
}
