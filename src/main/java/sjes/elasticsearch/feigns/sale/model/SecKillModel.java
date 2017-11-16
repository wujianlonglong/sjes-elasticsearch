package sjes.elasticsearch.feigns.sale.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀对象
 * Created by kimiyu on 17/1/6.
 */
@Data
public class SecKillModel implements Serializable {

    // 促销id
    private String saleId;

    // 秒杀价
    private BigDecimal salePrice;

    // 限购
    private Integer moreBuy = 1;

    // 库存
    private Integer stockNumber;
}
