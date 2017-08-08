package sjes.elasticsearch.feigns.order.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.order.model.ProductSales;

import java.util.List;

@FeignClient(Constants.SJES_API_ORDER)
@RequestMapping(value = "orders")
public interface SellFeign {

    @RequestMapping(value = "outservice/sellList", method = RequestMethod.GET)
    List<ProductSales> getSellList();
}
