package sjes.elasticsearch.feigns.sale.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.springframework.data.annotation.Id;
import sjes.elasticsearch.feigns.sale.common.SaleConstant;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 促销基础对象
 * Created by 白 on 2016/3/9.
 */
@Data
public class BaseModel implements Serializable {

    @Id
    private String id;
    /**
     * 促销名称
     */
    private String name = "";

    /**
     * 促销类型
     */
    private Integer saleType = 0;

    /**
     * 开始日期
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startDate;

    /**
     * 结束日期
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDate;

    /**
     * 使用场景
     */
    private List<Integer> envType = new ArrayList<>();

    /**
     * 会员等级
     */
    private List<Integer> memberLevel = new ArrayList<>();

    /**
     * 促销总笔数
     */
    private Integer saleTotalNumber = 0;

    /**
     * 参与门店状态: 1 所有门店;2 部分门店
     */
    private Integer joinGateShopType = 0;

    /**
     * 参与门店列表
     */
    private List<JoinedGateShop> gateShops = new ArrayList<>();

    /**
     * 备注
     */
    private String memo = "";

    /**
     * 换购优惠劵数量
     */
    private Integer limitVolumeNum = 0;

    /**
     * 要发消息的手机号
     */
    private List<String> telphoneStr = new ArrayList<>();

    /**
     * 消息发送的内容
     */
    private String smsContent = "";

    /**
     * 促销状态
     */
    private Integer status = SaleConstant.notBegin;

    /**
     * 创建人
     */
    private Long creater = 0L;

    /**
     * 创建日期
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdDate;

    /**
     * 更新人
     */
    private Long updater;

    /**
     * 更新日期
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedDate;
}
