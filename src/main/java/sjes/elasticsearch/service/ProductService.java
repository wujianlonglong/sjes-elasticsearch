package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.domain.ProductIndexModel;
import sjes.elasticsearch.feigns.item.feign.ProductFeign;
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
     * 根据分类Ids查询商品列表
     * @param categoryIds 分类Ids
     * @return 商品列表
     */
    @RequestMapping(value = "categoryIds", method = RequestMethod.POST)
    public List<ProductIndexModel> listByCategoryIds(List<Long> categoryIds) {
        List<ProductIndexModel> productIndexModels = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(categoryIds)) {
            List<List<Long>> categoryIdsList = ListUtils.splitList(categoryIds, Constants.SPLIT_SUB_LIST_SIZE);
            for (List<Long> cateIds : categoryIdsList) {
                productIndexModels.addAll(productFeign.listByCategoryIds(cateIds));
            }
        }
        return productIndexModels;
    }
}
