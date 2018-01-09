package sjes.elasticsearch.feigns.item.model;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
public class HomeCategoryRelation {

    private Long id; // 主键

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String erpGoodsId;

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String homeCategoryId;

    private Integer sort;
}
