package sjes.elasticsearch.domain;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

/**
 * Created by mac on 15/8/31.
 */
@Data
public class ProductAttributeValue implements Serializable {

    private Long productId; // 单品Id

    private Long attributeId; // 属性Id

    private String attributeName; //   属性名

    private Long attributeOptionId; // 属性选项Id

    private String attributeOptionName; // 属性选项名称

}
