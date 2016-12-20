package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.feigns.item.feign.ItemPriceFeign;
import sjes.elasticsearch.feigns.item.model.ItemPrice;
import sjes.elasticsearch.utils.ListUtils;

import java.util.List;

/**
 * Created by qinhailong on 16-12-9.
 */
@Service
public class ItemPriceService {

    @Autowired
    private ItemPriceFeign itemPriceFeign;

    /**
     * 根据商品管理码列表erpGoodsIds查询商品价格列表
     * @param erpGoodsIds 商品管理码列表erpGoodsIds
     * @return 商品价格列表
     */
    public List<ItemPrice> findByErpGoodsIdIn(List<Long> erpGoodsIds) {
        List<ItemPrice> itemPrices = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(erpGoodsIds)) {
            List<List<Long>> erpGoodsIdsList = ListUtils.splitList(erpGoodsIds, ListUtils.SPLIT_SUB_LIST_SIZE);
            for (List<Long> goodsIds : erpGoodsIdsList) {
                itemPrices.addAll(itemPriceFeign.findByErpGoodsIdIn(goodsIds));
            }
        }
        return itemPrices;
    }

    /**
     * 根据商品管理码erpGoodsId查询商品价格列表
     * @param erpGoodsId 商品管理码erpGoodsId
     * @return 商品价格列表
     */
    public List<ItemPrice> findByErpGoodsId(Long erpGoodsId) {
        return itemPriceFeign.findByErpGoodsId(erpGoodsId);
    }
}
