package sjes.elasticsearch.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.store.Store;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
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

    @Field(type = FieldType.Long)
    private Long id; // 主键

    @Field(type = FieldType.Long)
    private Long goodsId; // 主商品ID

    @Field(type = FieldType.Long)
    private Long erpGoodsId; // 对应ERP GoodsID

    @Field(type = FieldType.String)
    private String goodsCode; // 商品内码(顺序号)

    @Field(type = FieldType.String)
    private String sn; // 商品编码

    @Field(type = FieldType.String)
    private String barCode; // 商品条码

    @Field(indexAnalyzer = "ik", searchAnalyzer = "ik", type = FieldType.String)
    private String name; // 商品名称

    @Field(type = FieldType.Double)
    private Double salePrice; // 销售价

    @Field(type = FieldType.Double)
    private Double memberPrice; // 会员价

    @Field(type = FieldType.String)
    private String weight; // 商品重量

    @Field(indexAnalyzer = "ik", searchAnalyzer = "ik", type = FieldType.String)
    private String place; // 商品产地

    @Field(type = FieldType.String)
    private String source; // 商品来源, 枚举(自营/联营)

    @Field(type = FieldType.Long)
    private Long sales; //　销量

    @Field(type = FieldType.Long)
    private Long hits; // 点击数

    @Field(type = FieldType.Boolean)
    private Boolean isBargains; // 是否是惠商品

    @Field(type = FieldType.Integer)
    private Integer transportType; // 商品运输类型 常温:0 冷藏/冷冻:1 保热:2

    @Field(type = FieldType.Long)
    private Long categoryId; // 商品分类Id

    @Field(type = FieldType.Long)
    private Long brandId; // 品牌ID

    @Field(indexAnalyzer = "ik", searchAnalyzer = "ik", type = FieldType.String)
    private String brandName; // 品牌名称

    @Field(type = FieldType.Integer)
    private Integer status; // 0/1/2; 正常销售/下架停售/未审核

    @Field(type = FieldType.Long)
    private Long displaySales; // 展示销量

    @Field(type = FieldType.Long)
    private Long erpProductId; // ERP产品ID

    @Field(indexAnalyzer = "ik", searchAnalyzer = "ik", type = FieldType.String)
    private String adSlogan; // 广告语

    @Field(type = FieldType.Boolean)
    private Boolean isPromotionParticular; // 是否是促销例

    @Field(indexAnalyzer = "ik", searchAnalyzer = "ik", type = FieldType.String)
    private String introduction; // 介绍

    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Field(type = FieldType.Date)
    private LocalDateTime createDate; // 创建时间

    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Field(type = FieldType.Date)
    private LocalDateTime updateDate; // 更新时间


}
