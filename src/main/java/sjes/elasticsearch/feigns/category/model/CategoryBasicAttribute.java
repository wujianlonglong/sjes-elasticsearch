package sjes.elasticsearch.feigns.category.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by qinhailong on 15-10-28.
 */
@Data
public class CategoryBasicAttribute implements Serializable {

    private Long attributeId; // 属性id

    private String attributeName; // 属性名称

    private Integer orders; // 排序

    private List<CategoryBasicAttributeOption> categoryAttributeOptions;

}
