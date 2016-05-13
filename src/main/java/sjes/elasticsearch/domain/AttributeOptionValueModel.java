package sjes.elasticsearch.domain;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import sjes.elasticsearch.feigns.category.model.Attribute;
import sjes.elasticsearch.feigns.category.model.AttributeOption;

/**
 * Created by qinhailong on 15-12-8.
 */
@Data
public class AttributeOptionValueModel extends Attribute {

    @Field(type = FieldType.Nested)     //嵌套类型
    private AttributeOption attributeOption;
}
