package sjes.elasticsearch.feigns.store;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.feigns.store.model.StockViewModel;

import java.util.Map;

/**
 * Created by qinhailong on 16-7-4.
 */
@FeignClient("sjes-api-store")
@RequestMapping("/stocks")
public interface StockFeign {

    /**
     * 根据门店ID和ERPGoodsID列表批量获取库存列表
     *
     * @param stockViewModel
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/stockForList", consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<Long, Integer> stockForList(StockViewModel stockViewModel);
}
