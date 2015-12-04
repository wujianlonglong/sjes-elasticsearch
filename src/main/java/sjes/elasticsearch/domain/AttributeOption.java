package sjes.elasticsearch.domain;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;

/**
 * Created by mac on 15/8/27.
 */
@Data
@Document(indexName="attributeOptions", type="attributeOption")
public class AttributeOption implements Serializable {

    private Long id; // 主键

    private Long attributeId; // 商品分类属性id

    private String value; // 可选项值

    private Integer orders; // 排序

}
