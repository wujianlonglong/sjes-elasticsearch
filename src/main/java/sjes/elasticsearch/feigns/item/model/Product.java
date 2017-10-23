package sjes.elasticsearch.feigns.item.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;
import sjes.elasticsearch.serializer.CustomDateDeSerializer;
import sjes.elasticsearch.serializer.CustomDateSerializer;
import sjes.elasticsearch.serializer.MoneySerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity - 单品SKU
 * Created by qinhailong on 15-8-21.
 */
@Data
public class Product implements Serializable {

    @Id
    private Long id; // 主键

    private Long goodsId; // 主商品ID

    private Long erpGoodsId; // 对应ERP GoodsID

    //不分词,String类型
    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String searchStr;  //用于查询，"goodsId/erpGoodsId/sn/name"

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String goodsCode; // 商品内码(顺序号)

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String sn; // 商品编码

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String barCode; // 商品条码

    //索引时使用ik分词(即中文分词),搜索时使用ik分词,String类型
    @Field(indexAnalyzer = "ik", searchAnalyzer = "ik", type = FieldType.String)
    private String name; // 商品名称

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String namePinYin;  // 商品名称（拼音）

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String namePinYinAddr; // 商品名称（拼音首字母）

    //不索引
    @Field(index = FieldIndex.no, type = FieldType.String)
    private String displayName; // 展示的商品名称(高亮)

    @JsonSerialize(using = MoneySerializer.class)
    @Field(type = FieldType.Double)             //Double类型
    private BigDecimal salePrice; // 销售价

    @JsonSerialize(using = MoneySerializer.class)
    @Field(type = FieldType.Double)
    private BigDecimal originalSalePrice; // 原销售价

    @JsonSerialize(using = MoneySerializer.class)
    @Field(type = FieldType.Double)
    private BigDecimal memberPrice; // 会员价

    @JsonSerialize(using = MoneySerializer.class)
    @Field(type = FieldType.Double)
    private BigDecimal originalMemberPrice; // 原会员价

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String weight; // 商品重量

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String place; // 商品产地

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String source; // 商品来源, 枚举(自营/联营)

    private Long sales; //　销量

    private Long hits; // 点击数

    private Boolean isBargains; // 是否是惠商品

    private Boolean isSeckill; // 是否是秒杀

    private Boolean isFullGift; // 是否满赠

    private String remarker; // 备注

    private Integer transportType; // 商品运输类型 常温:0 冷藏/冷冻:1 保热:2

    private Long categoryId; // 商品分类Id

    private Long brandId; // 品牌ID

//    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
//    private String brandName; // 品牌名称

    private Integer status; // 0/1/2; 正常销售/下架停售/未审核

    private Long displaySales; // 展示销量

    private Long erpProductId; // ERP产品ID

    //  @Field(indexAnalyzer = "ik", searchAnalyzer = "ik", type = FieldType.String)
    private String adSlogan; // 广告语

    private Boolean isPromotionParticular; // 是否是促销例

    //分词,String类型
    @Field(indexAnalyzer = "ik", searchAnalyzer = "ik", type = FieldType.String)
    private String promotionType; // 促销类型

    //分词,String类型
    @Field(indexAnalyzer = "ik", searchAnalyzer = "ik", type = FieldType.String)
    private String promotionName; // 促销名称

    private String promotionShop;//促销门店

    // @Field(indexAnalyzer = "ik", searchAnalyzer = "ik", type = FieldType.String)
    private String introduction; // 介绍


    private Integer newFlag;//新品标志 0或null :不是新品，1：是新品

    private Integer saleType;

    /**
     * 促销价
     */
    private Double promotionPrice;
    /**
     * 促销的图标
     */
    private String saleHotTips;

    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDateTime groundingDate; //上架时间

    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDateTime createDate; // 创建时间

    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDateTime updateDate; // 更新时间
}
