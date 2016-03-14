package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.feigns.item.feign.ProductFeign;
import sjes.elasticsearch.feigns.item.model.ProductImageModel;
import sjes.elasticsearch.utils.ListUtils;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Service("productService")
public class ProductService {

    @Autowired
    private ProductFeign productFeign;

    /**
     * 根据productId得到指定的ProductImageModel
     * @param productId 商品id
     * @return  ProductImageModel
     */
    public ProductImageModel getProductImageModel(Long productId) {
        return productFeign.getProductImageModel(productId);
    }

    /**
     * 根据商品id列表查询 ProductsImageModel列表
     *
     * @param productIds 商品id列表
     * @return ProductsImageModel列表
     */
    public List<ProductImageModel> listProductsImageModel(List<Long> productIds) {
        return productFeign.listProductsImageModel(productIds);
    }

    /**
     * 根据商品id列表查询 ProductsImageModel列表
     *
     * @param sns 商品id列表
     * @return ProductsImageModel列表
     */
    public List<ProductImageModel> listBySns(List<String> sns) {
        return productFeign.listBySns(sns);
    }

    /**
     * 根据分类Ids查询商品列表
     * @param categoryIds 分类Ids
     * @return 商品列表
     */
    @RequestMapping(value = "categoryIds", method = RequestMethod.POST)
    public List<ProductImageModel> listByCategoryIds(List<Long> categoryIds) {
        List<ProductImageModel> productImageModels = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(categoryIds)) {
            List<List<Long>> categoryIdsList = ListUtils.splitList(categoryIds, ListUtils.SPLIT_SUB_LIST_SIZE);
            for (List<Long> cateIds : categoryIdsList) {
                productImageModels.addAll(productFeign.listByCategoryIds(cateIds));
            }
        }
        return productImageModels;
    }
}
