package sjes.elasticsearch.feigns.category.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by qinhailong on 15/9/14.
 */
@Data
public class Tag implements Serializable {

    private Long id; // 主键

    private Integer orders;  // 排序

    private String name;  // 标签名称

//    @JsonDeserialize(using = CustomDateDeSerializer.class)
//    @JsonSerialize(using = CustomDateSerializer.class)
//    private LocalDateTime createDate; // 创建时间
//
//    @JsonDeserialize(using = CustomDateDeSerializer.class)
//    @JsonSerialize(using = CustomDateSerializer.class)
//    private LocalDateTime updateDate; // 更新时间

}
