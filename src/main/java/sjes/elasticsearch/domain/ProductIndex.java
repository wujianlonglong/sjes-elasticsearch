package sjes.elasticsearch.domain;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import sjes.elasticsearch.feigns.category.model.Tag;
import sjes.elasticsearch.feigns.item.model.ProductImageModel;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Data
@Document(indexName = "sjes", type = "products")
public class ProductIndex extends ProductImageModel {

    /**
     * 商品分类属性值
     */
    @Field(type = FieldType.Nested)
    private List<AttributeOptionValueModel> attributeOptionValueModels;

    /**
     * 商品有关的标签
     */
    @Field(type = FieldType.Nested)
    private List<Tag> tags;

}
