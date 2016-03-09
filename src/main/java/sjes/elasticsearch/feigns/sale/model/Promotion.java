package sjes.elasticsearch.feigns.sale.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 促销(包含满赠促销和秒杀促销)
 * Created by 白 on 2016/3/9.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Promotion extends BaseModel implements Serializable {

    /**
     * 用户每天限买秒杀商品次数
     */
    private Integer limitBuyForSecondKill = 0;

    /**
     * 商品ID
     */
    private Long productId;
    /**
     * sn
     */
    private String sn;

    /**
     * 商品名称
     */
    private String itemName;

    /**
     * 图片
     */
    private String imageUrl;

    /**
     * 配送方式
     */
    private String allocationModel = "该活动仅支持三江配送";
    /**
     * 参与的赠品
     */
    private List<FullGift> fullGifts;

    private Integer joinedItemType;         // 参与方式[满赠]
    private List<JoinedItem> joinedItems;       // 参与的商品[满赠]
    @Digits(integer = 6,fraction = 3)
    private BigDecimal salePrice;                 // 促销价
    @Digits(integer = 6,fraction = 3)
    private BigDecimal sellPrice;                // 商品零售价
    @Digits(integer = 3,fraction = 3)
    private BigDecimal discount;                    // 折扣
    private Integer lessBuyNum;               // 单次最少购买数量
    private Integer moreBuyNum;               // 单次最多购买数量
    private String itemAd;                      // 商品广告语
    private String itemPromotionInfo;           // 商品促销信息
    private String itemDetonation;              // 商品爆照贴
    private String promotionStopReason;         //强停理由
}
