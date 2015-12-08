package sjes.elasticsearch.domain;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import sjes.elasticsearch.feigns.item.model.Product;
import sjes.elasticsearch.feigns.item.model.ProductAttributeValue;
import sjes.elasticsearch.feigns.item.model.ProductImage;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Data
public class ProductIndex extends Product {

    /**
     * 商品分类属性值
     */
    @Field(type= FieldType.Nested)
    private List<ProductAttributeValue> productAttributeValues;

    /**
     * 商品图片
     */
    private ProductImage productImage;

}
