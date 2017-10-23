package sjes.elasticsearch.feigns.order.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.order.model.ProductSales;

import java.util.List;

@FeignClient(Constants.SJES_API_ORDER)
@RequestMapping(value = "orders")
public interface SellFeign {

    /**
     * 获取商品销量
     * @param syncType 同步类型：0或空---增量同步；1---全量同步
     * @return
     */
    @RequestMapping(value = "outservice/sellList", method = RequestMethod.GET)
    List<ProductSales> getSellList(@RequestParam("syncType") int syncType);
}
