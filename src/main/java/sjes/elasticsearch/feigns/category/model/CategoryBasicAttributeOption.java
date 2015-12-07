package sjes.elasticsearch.feigns.category.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by qinhailong on 15-10-28.
 */
@Data
public class CategoryBasicAttributeOption implements Serializable {

    private Long attributeOptionId;

    private String attributeOptionName;

    private Integer orders; // 排序

}
