package sjes.elasticsearch.feigns.item.model;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


/**
 * Created by qinhailong on 15-11-10.
 */
@Data
public class ProductImageModelNew extends Product {

    /**
     * 商品图片
     *
     * 不分词,嵌套类型
     */
    @Field(type = FieldType.Nested)
    private ProductImage productImage;

}
