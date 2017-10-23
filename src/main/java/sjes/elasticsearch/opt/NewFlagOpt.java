package sjes.elasticsearch.opt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.service.SearchService;
import sjes.elasticsearch.serviceaxsh.SearchAxshService;

@Service
public class NewFlagOpt {

    @Autowired
    SearchAxshService searchAxshService;

    @Autowired
    SearchService searchService;

    /**
     * 更新是否为新品标志（上架两周内的为新品）--安鲜生活
     */
    @Scheduled(cron="0 0 1 * * ?")
    public void updateNewFlagAxsh(){
        searchAxshService.updateNewFlagAxsh();
    }



    /**
     * 更新是否为新品标志（上架两周内的为新品）--网购
     */
   // @Scheduled(cron="0 10 1 * * ?")
    public void updateNewFlagSjes(){
        searchService.updateNewFlagAxsh();
    }

}
