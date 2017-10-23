package sjes.elasticsearch.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by byinbo on 2017/8/7.
 */
@Data
public class ErpSaleGoodId {

    private String promotionType;

    private String promotionName;

    private Long goodsId;

    private String shopIds;

    private BigDecimal promotionPrice;

    private String saleHotTips;

    private Integer saleType;
}
