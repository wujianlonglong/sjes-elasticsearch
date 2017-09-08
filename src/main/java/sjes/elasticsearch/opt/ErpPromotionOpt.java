package sjes.elasticsearch.opt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.service.SearchService;
import sjes.elasticsearch.serviceaxsh.SearchAxshService;

@Service
public class ErpPromotionOpt {


    @Autowired
    SearchAxshService searchAxshService;


    @Autowired
    SearchService searchService;


    /**
     * 同步商品erp促销活动（网购）
     */
 //   @Scheduled(cron="0 10 3 * * *")
    public void syncErpPromotionSjes(){
        searchService.updatePromotion();//更新商品erp促销信息
    }

    /**
     * 同步商品erp促销活动（安鲜生活）
     */
//    @Scheduled(cron="0 20 3 * * *")
    public void syncErpPromotionAxsh(){
        searchAxshService.updatePromotion();//更新商品erp促销信息
    }

}
