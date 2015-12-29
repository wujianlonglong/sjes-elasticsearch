package sjes.elasticsearch.feigns.item.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by mac on 15/9/15.
 */
@Data
public class ProductCategory implements Serializable {

    private Long id; // 主键

    private Long productId; // 单品id

    private Long categoryId; // 分类id

}
