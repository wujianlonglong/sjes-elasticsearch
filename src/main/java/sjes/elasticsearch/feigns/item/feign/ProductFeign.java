package sjes.elasticsearch.feigns.item.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.item.model.Product;

import java.util.List;

/**
 * Created by mac on 15/8/28.
 */
@FeignClient(Constants.SJES_API_ITEM)
@RequestMapping(value = "products")
public interface ProductFeign {

    /**
     * 根据分类Ids查询商品列表
     * @param categoryIds 分类Ids
     * @return 商品列表
     */
    @RequestMapping(value = "categoryIds", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<Product> listByCategoryIds(List<Long> categoryIds);

}
