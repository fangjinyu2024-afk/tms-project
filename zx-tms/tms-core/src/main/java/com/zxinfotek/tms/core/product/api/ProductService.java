package com.zxinfotek.tms.core.product.api;

import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.core.product.api.model.ProductQuery;
import com.zxinfotek.tms.core.product.api.model.ProductSaveRequest;
import com.zxinfotek.tms.core.product.api.model.ProductVO;

public interface ProductService {

    PageResult<ProductVO> page(ProductQuery query);

    ProductVO detail(Long id);

    Long create(ProductSaveRequest request);

    void update(Long id, ProductSaveRequest request);

    void delete(Long id);

    String export(ProductQuery query);
}
