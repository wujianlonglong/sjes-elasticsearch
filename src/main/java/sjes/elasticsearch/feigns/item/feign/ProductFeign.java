package sjes.elasticsearch.feigns.item.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.item.model.ProductImageModel;

import java.util.List;

/**
 * Created by qinhailong on 15/8/28.
 */
@FeignClient(Constants.SJES_API_ITEM)
@RequestMapping(value = "products")
public interface ProductFeign {

    /**
     * 根据productId得到指定的ProductImageModel
     * @param productId 商品id
     * @return  ProductImageModel
     */
    @RequestMapping(value = "image/{productId}", method = RequestMethod.GET)
    ProductImageModel getProductImageModel(@PathVariable("productId") Long productId);

    /**
     * 根据分类Ids查询商品列表
     * @param categoryIds 分类Ids
     * @return 商品列表
     */
    @RequestMapping(value = "categoryIds", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<ProductImageModel> listByCategoryIds(List<Long> categoryIds);

}
