package sjes.elasticsearch.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSales {

    /**
     * 商品编号
     */
    private Long goodId;

    /**
     * 销售数量
     */
    private Long saleNum;

    /**
     * 平台号
     */
    private String platId;
}
