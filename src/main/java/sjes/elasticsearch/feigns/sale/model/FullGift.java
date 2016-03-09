package sjes.elasticsearch.feigns.sale.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by 白 on 2016/3/9
 */
@Data
public class FullGift implements Serializable {
    /**
     * 主键
     */
    @Id
    private String id;
    /**
     * 促销ID
     */
    private String saleId;
    /**
     * 会员满
     */
    private BigDecimal memberMoreThan = new BigDecimal(0.00);

    /**
     * 惠用户满
     */
    private BigDecimal benefitUserMoreThan = new BigDecimal(0.00);
    /**
     * 促销上限
     */
    private Integer limitNumber;
    /**
     * 加入的赠品列表
     */
    private List<JoinedGift> joinedGiftList;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 满赠阶梯
     */
    private Integer giftLevel;

}
