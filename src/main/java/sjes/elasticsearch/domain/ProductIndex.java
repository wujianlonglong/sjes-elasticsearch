package sjes.elasticsearch.domain;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import sjes.elasticsearch.feigns.category.model.Tag;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Data
public class ProductIndex extends ProductIndexModel {

    /**
     * 商品分类属性值
     */
    @Field(type= FieldType.Nested)
    private List<AttributeOptionValueModel> attributeOptionValueModels;

    /**
     * 商品有关的标签
     */
    private List<Tag> tags;

}
