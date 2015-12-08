package sjes.elasticsearch.feigns.item.model;

import lombok.Data;

/**
 * Created by qinhailong on 15-11-10.
 */
@Data
public class ProductImageModel extends Product {

    /**
     * 商品图片
     */
    private ProductImage productImage;

}
