package sjes.elasticsearch.feigns.item.feignaxsh;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.item.model.ItemPrice;

import java.util.List;

/**
 * Created by qinhailong on 16-12-9.
 */
@FeignClient(Constants.AXSH_API_ITEM)
@RequestMapping(value = "itemPrices/anxian")
public interface ItemPriceAxshFeign {

    /**
     * 根据商品管理码列表erpGoodsIds查询商品价格列表
     *
     * @param erpGoodsIds 商品管理码列表erpGoodsIds
     * @return 商品价格列表
     */
    @RequestMapping(value = "findIn", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<ItemPrice> findByErpGoodsIdIn(List<Long> erpGoodsIds);

    @RequestMapping(value = "findByErpGoodsIdInAndStatus", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<ItemPrice> findByErpGoodsIdInAndStatus(List<Long> erpGoodsIds);

    /**
     * 根据商品管理码erpGoodsId查询商品价格列表
     *
     * @param erpGoodsId 商品管理码erpGoodsId
     * @return 商品价格列表
     */
    @RequestMapping(value = "findByErpGoodsId", method = RequestMethod.GET)
    List<ItemPrice> findByErpGoodsId(@RequestParam("erpGoodsId") Long erpGoodsId);

    @RequestMapping(value = "findByErpGoodsIdAndStatus", method = RequestMethod.GET)
    List<ItemPrice> findByErpGoodsIdAndStatus(@RequestParam("erpGoodsId") Long erpGoodsId);

}
