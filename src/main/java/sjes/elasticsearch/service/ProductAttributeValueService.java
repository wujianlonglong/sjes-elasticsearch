package sjes.elasticsearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.feigns.item.feign.ProductAttributeValueFeign;
import sjes.elasticsearch.feigns.item.model.ProductAttributeValue;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Service("productAttributeValueService")
public class ProductAttributeValueService {

    @Autowired
    private ProductAttributeValueFeign productAttributeValueFeign;

    /**
     * 根据商品Ids查询商品属性值列表
     * @param productIds 商品Ids
     * @return 商品列表
     */
    public List<ProductAttributeValue> listByProductIds(List<Long> productIds) {
        return productAttributeValueFeign.listByProductIds(productIds);
    }
}
