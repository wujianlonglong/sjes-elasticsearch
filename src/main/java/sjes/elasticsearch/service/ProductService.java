package sjes.elasticsearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.feigns.item.feign.ProductFeign;
import sjes.elasticsearch.feigns.item.model.Product;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Service("productService")
public class ProductService {

    @Autowired
    private ProductFeign productFeign;

    /**
     * 根据分类Ids查询商品列表
     * @param categoryIds 分类Ids
     * @return 商品列表
     */
    @RequestMapping(value = "categoryIds", method = RequestMethod.POST)
    public List<Product> listByCategoryIds(List<Long> categoryIds) {
        return productFeign.listByCategoryIds(categoryIds);
    }
}
