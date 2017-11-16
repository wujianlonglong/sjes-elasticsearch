package sjes.elasticsearch.feigns.sale.feign;


import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.sale.model.PromotionDTO;
import sjes.elasticsearch.feigns.sale.model.SaleResponseMessage;
import sjes.elasticsearch.feigns.sale.model.SecKillModel;

import java.util.Map;
import java.util.Set;

@FeignClient(Constants.SJES_API_SALE)
@RequestMapping(value = "/anxian/promotions", consumes = MediaType.APPLICATION_JSON_VALUE)
public interface PromotionsFeign {

    /**
     * 获取有效的促销秒杀列表
     *
     * @param promotionDTO
     * @return
     */
    @RequestMapping(value = "/availableSecKillPromotions", method = RequestMethod.POST)
    SaleResponseMessage<Map<Long, SecKillModel>> getSecKillPromotions( PromotionDTO<Set<Long>> promotionDTO);

}
