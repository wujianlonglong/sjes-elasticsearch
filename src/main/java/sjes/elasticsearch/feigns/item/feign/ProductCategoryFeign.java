package sjes.elasticsearch.feigns.item.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.item.model.ProductCategory;

import java.util.List;

/**
 * Created by qinhailong on 15-12-25.
 */
@FeignClient(Constants.SJES_API_ITEM)
@RequestMapping("productCategorys")
public interface ProductCategoryFeign {

    /**
     * 所有商品多规格分类
     * @return 分类列表
     */
    @RequestMapping(method = RequestMethod.GET)
    List<ProductCategory> listAll();

}
