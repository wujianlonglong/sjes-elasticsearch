package sjes.elasticsearch.feigns.item.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by qinhailong on 15/8/31.
 */
@Data
public class ProductAttributeValue implements Serializable {

    private Long id; // 主键

    private Long productId; // 单品Id

    private Long attributeId; // 属性Id

    private String attributeName; //   属性名

    private Long attributeOptionId; // 属性选项Id

    private String attributeOptionName; // 属性选项名称

    private String attributeValue; // 属性选项值

}
