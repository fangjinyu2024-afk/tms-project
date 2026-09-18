package com.zxinfotek.tms.core.product.api.model;

import com.zxinfotek.tms.common.enums.ProductCategory;
import com.zxinfotek.tms.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductQuery extends PageQuery {

    private String keyword;
    private ProductCategory category;
}
