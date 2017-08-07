package sjes.elasticsearch.feigns.item.feignaxsh;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.item.model.ProductCategory;

import java.util.List;

/**
 * Created by qinhailong on 15-12-25.
 */
@FeignClient(Constants.AXSH_API_ITEM)
@RequestMapping("productCategorys/anxian")
public interface ProductCategoryAxshFeign {

    /**
     * 所有商品多规格分类
     * @return 分类列表
     */
    @RequestMapping(method = RequestMethod.GET)
    List<ProductCategory> listAll();

    /**
     * 根据商品Id得到分类列表
     * @param productId 商品Id
     * @return 分类列表
     */
    @RequestMapping(value = "productCategorys/{productId}", method = RequestMethod.GET)
    List<ProductCategory> findProductCategorysByProductId(@PathVariable("productId") Long productId);

}
