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

import java.io.Serializable;
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

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String goodsCode; // 商品内码(顺序号)

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String sn; // 商品编码

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String barCode; // 商品条码

    @Field(indexAnalyzer = "ik", searchAnalyzer = "ik", type = FieldType.String)
    private String name; // 商品名称

    @Field(index = FieldIndex.no,type = FieldType.String)
    private String displayName; // 展示的商品名称(高亮)

    private Double salePrice; // 销售价

    private Double originalSalePrice; // 原销售价

    private Double memberPrice; // 会员价

    private Double originalMemberPrice; // 原会员价

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String weight; // 商品重量

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String place; // 商品产地

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String source; // 商品来源, 枚举(自营/联营)

    private Long sales; //　销量

    private Long hits; // 点击数

    private Boolean isBargains; // 是否是惠商品

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

    private Integer promotionType; // 促销类型

   // @Field(indexAnalyzer = "ik", searchAnalyzer = "ik", type = FieldType.String)
    private String introduction; // 介绍

    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDateTime createDate; // 创建时间

    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDateTime updateDate; // 更新时间
}
