package sjes.elasticsearch.feigns.item.model;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


/**
 * Created by qinhailong on 15-11-10.
 */
@Data
public class ProductImageModel extends Product {

    /**
     * 商品图片(不分析)
     */
    @Field(type = FieldType.Nested)
    private ProductImage productImage;

}
