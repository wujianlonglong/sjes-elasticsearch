package sjes.elasticsearch.domainaxsh;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;
import sjes.elasticsearch.serializer.CustomDateDeSerializer;
import sjes.elasticsearch.serializer.CustomDateSerializer;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by 白 on 2015/12/21.
 */
@Data
@Document(indexName = "logstash-axsh", type = "searchlog")      //索引(index)名称:logstash-axsh,映射(mapping)名称:searchlog
public class SearchLogModelAxsh implements Serializable {

    @Id
    private Long id; // 主键

    //keyword在mapping中为不分词,String类型的字段
    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String keyword;         // 搜索内容

    private Long categoryId;

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String shopId;

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String sortType;

    private Double startPrice;

    private Double endPrice;

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String userAgent;

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String ip;

    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Field(type = FieldType.Date)                       //时间戳类型
    private LocalDateTime createDate; // 创建时间
}
