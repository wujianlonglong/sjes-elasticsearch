package sjes.elasticsearch.domain;

import lombok.Data;
import sjes.elasticsearch.feigns.item.model.Product;
import sjes.elasticsearch.feigns.item.model.ProductImage;
import sjes.elasticsearch.feigns.item.model.ProductTag;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Data
public class ProductIndexModel extends Product {

    /**
     * 商品图片(不分析)
     */
    private ProductImage productImage;

    /**
     * 商品关联的标签 (不存储，不分析)
     */
    private List<ProductTag> productTags;

}
