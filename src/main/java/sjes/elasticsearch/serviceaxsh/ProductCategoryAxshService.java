package sjes.elasticsearch.serviceaxsh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sjes.elasticsearch.feigns.item.feignaxsh.ProductCategoryAxshFeign;
import sjes.elasticsearch.feigns.item.model.ProductCategory;

import java.util.List;

/**
 * Created by qinhailong on 15-12-29.
 */
@Service("productCategoryAxshService")
public class ProductCategoryAxshService {

    @Autowired
    private ProductCategoryAxshFeign productCategoryAxshFeign;

    /**
     * 所有商品多规格分类
     * @return 分类列表
     */
    public List<ProductCategory> listAll() {
        return productCategoryAxshFeign.listAll();
    }

    /**
     * 根据商品Id得到分类列表
     * @param productId 商品Id
     * @return 分类列表
     */
    public List<ProductCategory> findProductCategorysByProductId( Long productId) {
        return productCategoryAxshFeign.findProductCategorysByProductId(productId);
    }
}
