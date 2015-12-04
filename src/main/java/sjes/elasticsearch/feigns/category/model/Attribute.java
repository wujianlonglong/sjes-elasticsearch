package sjes.elasticsearch.feigns.category.model;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;

/**
 * Created by mac on 15/8/27.
 */
@Data
@Document(indexName="attributes", type="attribute")
public class Attribute implements Serializable {

    private Long id; // 主键

    private String name; // 属性名称

    private Long categoryAttributeGroupId; // 属性组Id

    private Integer orders; // 排序

    private Integer type; // 属性类型: 0: 下拉属性值； 1：可填写属性

    private String unit; // 属性单位

    private Boolean isFilter; // 是否加入分类筛选项

}
