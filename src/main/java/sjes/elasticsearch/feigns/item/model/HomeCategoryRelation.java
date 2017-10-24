package sjes.elasticsearch.feigns.item.model;

import lombok.Data;

@Data
public class HomeCategoryRelation {

    private Long id; // 主键

    private String erpGoodsId;

    private String homeCategoryId;
}
