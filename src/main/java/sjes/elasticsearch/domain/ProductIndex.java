package sjes.elasticsearch.domain;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;
import sjes.elasticsearch.feigns.category.model.Tag;
import sjes.elasticsearch.feigns.item.model.ProductImageModel;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Data
@Document(indexName = "sjes", type = "products")        //索引(index)名称:sjes,映射(mapping)名称:products
public class ProductIndex extends ProductImageModel {

    /**
     * 商品分类属性值
     *
     * 该字段在mapping中:嵌套类型
     */
    @Field(type = FieldType.Nested)
    private List<AttributeOptionValueModel> attributeOptionValueModels;

    /**
     * 商品有关的标签
     */
    @Field(type = FieldType.Nested)
    private List<Tag> tags;

    /**
     * 多分类ids
     */
    private List<String> productCategoryIds;

    /**
     * 品牌
     *
     * 该字段在mapping中:不分词,String类型
     */
    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String brandName;

}
