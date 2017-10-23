package sjes.elasticsearch.serviceaxsh;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sjes.elasticsearch.feigns.item.feignaxsh.ProductAxshFeign;
import sjes.elasticsearch.feigns.item.model.ProductImageModel;
import sjes.elasticsearch.utils.ListUtils;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Service("productAxshService")
public class ProductAxshService {

    @Autowired
    private ProductAxshFeign productAxshFeign;

    /**
     * 根据productId得到指定的ProductImageModel
     * @param productId 商品id
     * @return  ProductImageModel
     */
    public ProductImageModel getProductImageModel(Long productId) {
        return productAxshFeign.getProductImageModel(productId);
    }

    /**
     * 根据商品id列表查询 ProductsImageModel列表
     *
     * @param productIds 商品id列表
     * @return ProductsImageModel列表
     */
    public List<ProductImageModel> listProductsImageModel(List<Long> productIds) {
        return productAxshFeign.listProductsImageModel(productIds);
    }

    /**
     * 根据商品id列表查询 ProductsImageModel列表
     *
     * @param sns 商品id列表
     * @return ProductsImageModel列表
     */
    public List<ProductImageModel> listBySns(List<String> sns) {
        return productAxshFeign.listBySns(sns);
    }

    /**
     * 根据分类Ids查询商品列表
     * @param categoryIds 分类Ids
     * @return 商品列表
     */
    public List<ProductImageModel> listByCategoryIds(List<Long> categoryIds) {
        List<ProductImageModel> productImageModels = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(categoryIds)) {
            List<List<Long>> categoryIdsList = ListUtils.splitList(categoryIds, ListUtils.SPLIT_SUB_LIST_SIZE);
            for (List<Long> cateIds : categoryIdsList) {
                productImageModels.addAll(productAxshFeign.listByCategoryIds(cateIds));
            }
        }
        return productImageModels;
    }
}
