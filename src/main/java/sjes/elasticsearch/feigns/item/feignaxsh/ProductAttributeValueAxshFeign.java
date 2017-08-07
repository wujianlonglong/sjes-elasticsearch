package sjes.elasticsearch.feigns.item.feignaxsh;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.item.model.ProductAttributeValue;

import java.util.List;

/**
 * Created by qinhailong on 15-11-17.
 */
@FeignClient(Constants.AXSH_API_ITEM)
@RequestMapping("productAttributeValues/anxian")
public interface ProductAttributeValueAxshFeign {

    /**
     * 根据商品Ids查询商品属性值列表
     * @param productIds 商品Ids
     * @return 商品列表
     */
    @RequestMapping(value = "list", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<ProductAttributeValue> listByProductIds(List<Long> productIds);

}
