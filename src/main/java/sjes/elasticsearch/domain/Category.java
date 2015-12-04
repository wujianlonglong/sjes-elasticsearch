package sjes.elasticsearch.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
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
public class Category implements Serializable {

    private String id; // 主键

    private String name; // 分类名称

    private Long parentId; // 父分类ID

    private Integer grade; // 级别

    private String treePath; // 路径

    private Integer orders; // 排序

    private Boolean display; // 是否显示

    private String speciHref; // 专题页链接

    private Boolean isRedLabel; // 文本标红

    private String tagName; // 分类标签名

    private String classes; // 样式

    private Long seoId; // seoId

    @CreatedDate
    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDateTime createDate; // 创建时间

    @LastModifiedDate
    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDateTime updateDate; // 更新时间

}
