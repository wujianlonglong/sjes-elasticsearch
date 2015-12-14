package sjes.elasticsearch.feigns.category.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javafx.util.converter.FloatStringConverter;
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
 * 商品分类表
 * @author qinhailong
 */
@Data
@Document(indexName = "sjes", type = "categories")
public class Category implements Serializable {

    @Id
    private Long id; // 主键

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String name; // 分类名称

    private Long parentId; // 父分类ID

    private Integer grade; // 级别

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String treePath; // 路径

    private Integer orders; // 排序

    private Boolean display; // 是否显示

    private String speciHref; // 专题页链接

    private Boolean isRedLabel; // 文本标红

    private String tagName; // 分类标签名

    private String classes; // 样式

    private Long seoId; // seoId

    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDateTime createDate; // 创建时间

    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDateTime updateDate; // 更新时间

}
