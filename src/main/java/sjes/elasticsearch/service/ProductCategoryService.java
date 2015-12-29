package sjes.elasticsearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.feigns.item.feign.ProductCategoryFeign;
import sjes.elasticsearch.feigns.item.model.ProductCategory;

import java.util.List;

/**
 * Created by qinhailong on 15-12-29.
 */
@Service("productCategoryService")
public class ProductCategoryService {

    @Autowired
    private ProductCategoryFeign productCategoryFeign;

    /**
     * 所有商品多规格分类
     * @return 分类列表
     */
    public List<ProductCategory> listAll() {
        return productCategoryFeign.listAll();
    }
}
