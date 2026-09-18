package com.zxinfotek.tms.core.product.api.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductVO {

    private Long id;
    private String category;
    private String categoryLabel;
    private String name;
    private String imagePath;
    private String description;
    private List<ProductModelVO> models;
    private LocalDateTime createTime;
}
