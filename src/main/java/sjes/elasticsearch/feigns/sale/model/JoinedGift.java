package sjes.elasticsearch.feigns.sale.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * 参与的赠品对象
 * Created by 白 on 2016/3/9.
 */
@Data
public class JoinedGift implements Serializable {
    @Id
    private String id;
    /**
     * 添加人ID
     */
    private Long userId;
    /**
     * 赠品编码
     */
    private String giftCode;
    /**
     * 赠品名称
     */
    private String giftName;
    /**
     * 赠品总量
     */
    private Integer giftAmount;
    /**
     * 每单赠送数量
     */
    private Integer sendNumber;
    /**
     * 赠品状态
     */
    private Integer giftStatus;
    /**
     * 赠品层级
     */
    private Integer giftLevel;
}
