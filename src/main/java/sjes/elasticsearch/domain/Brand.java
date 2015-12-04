package sjes.elasticsearch.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.Document;
import sjes.elasticsearch.serializer.CustomDateDeSerializer;
import sjes.elasticsearch.serializer.CustomDateSerializer;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by mac on 15/9/15.
 */
@Data
@Document(indexName="brands", type="brand")
public class Brand implements Serializable {

    private Long id; // 主键

    private String name; // 品牌名称

    private String logo; // logo

    private String url; // URL

    private Integer orders; // 排序

    @CreatedDate
    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDateTime createDate; // 创建时间

    @LastModifiedDate
    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDateTime updateDate; // 更新时间
}
