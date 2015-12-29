package sjes.elasticsearch.feigns.item.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import sjes.elasticsearch.serializer.CustomDateDeSerializer;
import sjes.elasticsearch.serializer.CustomDateSerializer;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by qinhailong on 15/9/15.
 */
@Data
public class Brand implements Serializable {

    private Long id; // 主键

    private Long brandId; // 老系统品牌id

    private String name; // 品牌名称

    private String logo; // logo

    private String url; // URL

    private Integer orders; // 排序

    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDateTime createDate; // 创建时间

    @JsonDeserialize(using = CustomDateDeSerializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private LocalDateTime updateDate; // 更新时间
}
