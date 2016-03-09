package sjes.elasticsearch.feigns.sale.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.sale.model.Promotion;

import java.util.List;

/**
 * Created by 白 on 2016/3/9.
 */
@FeignClient(Constants.SJES_API_SALE)
@RequestMapping(value = "/sales/promotion", consumes = MediaType.APPLICATION_JSON_VALUE)
public interface PromotionFeign {

    /**
     * 根据秒杀促销类型获取秒杀商品列表
     *
     * @return
     */
    @RequestMapping(value = "/productIdForSecondKill", method = RequestMethod.GET)
    List<Promotion> productIdsForSecondKill(@RequestParam("saleStatus") Integer saleStatus);
}
