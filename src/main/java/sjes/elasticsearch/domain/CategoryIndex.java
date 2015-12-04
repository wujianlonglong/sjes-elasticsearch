package sjes.elasticsearch.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * Created by qinhailong on 15-12-3.
 */
@Data
@Document(indexName = "categoryIndexes", type = "categoryIndex")
public class CategoryIndex extends Category {

//    @Id
//    @Field(type = FieldType.String)
//    private String id; // 主键

    // 商品列表
    @Field(type= FieldType.Nested)
    private List<ProductIndex> productIndexes;

}
