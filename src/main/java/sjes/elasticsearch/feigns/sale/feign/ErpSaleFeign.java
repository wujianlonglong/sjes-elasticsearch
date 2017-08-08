package sjes.elasticsearch.feigns.sale.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.domain.ErpSaleGoodId;

import java.util.List;

@FeignClient(Constants.SJES_API_SALE)
@RequestMapping(value = "/sales/erp", consumes = MediaType.APPLICATION_JSON_VALUE)
public interface ErpSaleFeign {

    @RequestMapping(value = "/goodsId", method = RequestMethod.GET)
     List<ErpSaleGoodId> getErpSaleGoods();
}
