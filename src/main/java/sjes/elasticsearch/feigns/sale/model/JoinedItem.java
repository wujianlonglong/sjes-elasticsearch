package sjes.elasticsearch.feigns.sale.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * Created by 白 on 2016/3/9
 */
@Data
public class JoinedItem implements Serializable {
    @Id
    private String id;
    /**
     * 用户名
     */
    private Long userId;
    /**
     * 参与方式类型
     */
    private Integer participationMode;
    /**
     * 卷类型
     */
    private Integer saleType;
    /**
     * 商品ID
     */
    private Long productId;
    /**
     * 商品Sn编码
     */
    private String sn;
    /**
     * 商品名称
     */
    private String productName;

    /**
     * 是否为特例商品[默认否]
     */
    private Boolean specProduct = Boolean.FALSE;
}
