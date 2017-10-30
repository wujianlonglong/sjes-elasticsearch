package sjes.elasticsearch.feigns.hubapi.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("sjes-hub-api")
@RequestMapping("gateShop")
public interface GateShopFeign {

    @RequestMapping(value = "/getAllShopIds", method = RequestMethod.GET)
     List<String> getAllShopIds(@RequestParam("platform") int platform) ;
}
