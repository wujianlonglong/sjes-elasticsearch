package sjes.elasticsearch.domain;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Document(indexName = "productIndexes", type = "productIndex")
public class ProductIndex extends Product {

    // 商品分类属性值
    @Field(type= FieldType.Nested)
    private List<ProductAttributeValue> productAttributeValues;

}
