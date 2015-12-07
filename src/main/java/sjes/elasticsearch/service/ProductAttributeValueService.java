package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.item.feign.ProductAttributeValueFeign;
import sjes.elasticsearch.feigns.item.model.ProductAttributeValue;
import sjes.elasticsearch.utils.ListUtils;

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
        List<ProductAttributeValue> productAttributeValues = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(productAttributeValues)) {
            List<List<Long>> productIdsList = ListUtils.splitList(productIds, Constants.SPLIT_SUB_LIST_SIZE);
            for (List<Long> proIds : productIdsList) {
                productAttributeValues.addAll(productAttributeValueFeign.listByProductIds(proIds));
            }
        }
        return productAttributeValues;
    }
}
