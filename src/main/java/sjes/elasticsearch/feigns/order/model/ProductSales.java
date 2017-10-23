package sjes.elasticsearch.feigns.order.model;

import lombok.Data;

@Data
public class ProductSales {

    /**
     * 商品编号
     */
    private Long erpGoodsId;

    /**
     * 销售数量
     */
    private Long saleNum;

    /**
     * 平台号
     */
    private String platId;
}
