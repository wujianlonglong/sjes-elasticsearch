package sjes.elasticsearch.domainaxsh;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import sjes.elasticsearch.domain.AttributeOptionValueModel;
import sjes.elasticsearch.feigns.category.model.Tag;
import sjes.elasticsearch.feigns.item.model.ItemPrice;
import sjes.elasticsearch.feigns.item.model.ProductImageModel;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Data
@Document(indexName = "axsh", type = "products")        //索引(index)名称:cxll,映射(mapping)名称:products
public class ProductIndexAxsh extends ProductImageModel {

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
    @Field(type = FieldType.String)
    private List<String> productCategoryIds;

    /**
     * 首页分类
     */
    @Field(type=FieldType.String)
    private List<String> homeCategoryIds;

    /**
     * 品牌
     *
     * 该字段在mapping中:不分词,String类型
     */
    @Field( type = FieldType.String)
    private String brandName;

    /**
     * 商品价格列表
     */
    @Field(type = FieldType.Nested)
    private List<ItemPrice> itemPrices = Lists.newArrayList();

}
