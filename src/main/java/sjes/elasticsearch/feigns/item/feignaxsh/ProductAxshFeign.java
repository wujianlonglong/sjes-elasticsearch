package sjes.elasticsearch.feigns.item.feignaxsh;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.item.model.HomeCategoryRelation;
import sjes.elasticsearch.feigns.item.model.ProductImageModel;

import java.util.List;

/**
 * Created by qinhailong on 15/8/28.
 */
@FeignClient(Constants.AXSH_API_ITEM)
@RequestMapping(value = "products/anxian")
public interface ProductAxshFeign {

    /**
     * 根据productId得到指定的ProductImageModel
     * @param productId 商品id
     * @return  ProductImageModel
     */
    @RequestMapping(value = "image/{productId}", method = RequestMethod.GET)
    ProductImageModel getProductImageModel(@PathVariable("productId") Long productId);

    /**
     * 根据商品id列表查询 ProductsImageModel列表
     *
     * @param productIds 商品id列表
     * @return ProductsImageModel列表
     */
    @RequestMapping(value = "list/images", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<ProductImageModel> listProductsImageModel(List<Long> productIds);

    /**
     * 根据商品id列表查询 ProductsImageModel列表
     *
     * @param sns 商品id列表
     * @return ProductsImageModel列表
     */
    @RequestMapping(value = "listBySns/images", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<ProductImageModel> listBySns(List<String> sns);

    /**
     * 根据分类Ids查询商品列表
     * @param categoryIds 分类Ids
     * @return 商品列表
     */
    @RequestMapping(value = "categoryIds", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<ProductImageModel> listByCategoryIds(List<Long> categoryIds);

    @RequestMapping(value = "categoryIdsnew", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<ProductImageModel> listByCategoryIdsnew(List<Long> categoryIds);

    @RequestMapping(value = "listByCategoryIdsCateNum", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<ProductImageModel> listByCategoryIdsCateNum(@RequestParam("shopId") String shopId, List<Long> categoryIds);


    /**
     * 查询首页分类-商品关系数据
     * @return
     */
    @RequestMapping(value="/findAllHomeCategoryRelation",method=RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
     List<HomeCategoryRelation> findAllHomeCategoryRelation();


}
