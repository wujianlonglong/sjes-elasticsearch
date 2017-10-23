package sjes.elasticsearch.serviceaxsh;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sjes.elasticsearch.feigns.item.feignaxsh.ProductAttributeValueAxshFeign;
import sjes.elasticsearch.feigns.item.model.ProductAttributeValue;
import sjes.elasticsearch.utils.ListUtils;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Service("productAttributeValueAxshService")
public class ProductAttributeValueAxshService {

    @Autowired
    private ProductAttributeValueAxshFeign productAttributeValueAxshFeign;

    /**
     * 根据商品Ids查询商品属性值列表
     * @param productIds 商品Ids
     * @return 商品列表
     */
    public List<ProductAttributeValue> listByProductIds(List<Long> productIds) {
        List<ProductAttributeValue> productAttributeValues = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(productIds)) {
            List<List<Long>> productIdsList = ListUtils.splitList(productIds, ListUtils.SPLIT_SUB_LIST_SIZE);
            for (List<Long> proIds : productIdsList) {
                productAttributeValues.addAll(productAttributeValueAxshFeign.listByProductIds(proIds));
            }
        }
        return productAttributeValues;
    }
}
