package sjes.elasticsearch.feigns.item.model;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;

/**
 * Created by mac on 15/9/21.
 */
@Data
@Document(indexName="productTags", type="productTag")
public class ProductTag implements Serializable {

    private Long id; // 主键

    private Long tagId; //  标签id

    private Long productId; // 单品id
}
