package sjes.elasticsearch.domain;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import sjes.elasticsearch.feigns.item.model.Product;
import sjes.elasticsearch.feigns.item.model.ProductImage;

/**
 * Created by qinhailong on 15-12-4.
 */
@Data
public class ProductIndexModel extends Product {

    /**
     * 商品图片
     *
     * 该字段在mapping中:不分词,嵌套类型
     */
    @Field(type = FieldType.Nested)
    private ProductImage productImage;

}
