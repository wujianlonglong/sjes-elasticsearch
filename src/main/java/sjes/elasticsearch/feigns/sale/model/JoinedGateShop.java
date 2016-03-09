package sjes.elasticsearch.feigns.sale.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sjes.elasticsearch.feigns.sale.common.SaleConstant;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 参与门店对象
 * Created by 白 on 2016/3/9
 */
@Data
public class JoinedGateShop implements Serializable {

    @Id
    private String id;
    /**
     * 用户ID
     */
    private Long userId = 0L;

    /**
     * 促销类型
     */
    private Integer saleType = 0;

    /**
     * 促销ID
     */
    private String saleId = "";

    /**
     * 赠品列表
     */
    private Map<String, JoinedGift> joinedGifts = new HashMap<>();
    /**
     * 县市区名称
     */
    private String areaName;

    /**
     * 门店编号
     */
    private String serialNumber = "";

    /**
     * 门店名称
     */
    private String shopName = "";

    /**
     * 门店状态【默认正常】
     */
    private Boolean status = SaleConstant.NORMAL;

}
