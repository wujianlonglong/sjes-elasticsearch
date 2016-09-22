package sjes.elasticsearch.service;

import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.feigns.store.StockFeign;
import sjes.elasticsearch.feigns.store.model.StockViewModel;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by qinhailong on 16-7-4.
 */
@Service
public class StockService {

    @Autowired
    private StockFeign stockFeign;

    /**
     * 根据门店ID和ERPGoodsID列表批量获取库存列表
     * @param shopId 门店ID
     * @param goodsIdList ERPGoodsID列表
     * @return
     */
    public Map<Long, Integer> stockForList(String shopId, List<Long> goodsIdList) {
        Objects.requireNonNull(shopId, "门店ID不能为空");
        if (CollectionUtils.isNotEmpty(goodsIdList)) {
            StockViewModel stockViewModel = new StockViewModel();
            stockViewModel.setShopId(shopId);
            stockViewModel.setGoodsIdList(goodsIdList);
            return stockFeign.stockForList(stockViewModel);
        }
        return Maps.newHashMap();
    }
}
