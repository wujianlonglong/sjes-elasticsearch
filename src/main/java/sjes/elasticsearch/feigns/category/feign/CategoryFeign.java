package sjes.elasticsearch.feigns.category.feign;


import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sjes.elasticsearch.common.ResponseMessage;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.category.model.Category;

import java.util.List;
import java.util.Map;

/**
 * Created by qinhailong on 15/8/26.
 */
@FeignClient(name=Constants.SJES_API_CATEGORY)
@RequestMapping("categorys")
public interface CategoryFeign {

    /**
     * 根据分类id查询分类簇对象
     *
     * @param categoryId 　分类id
     * @return　分类簇对象
     */
    @RequestMapping(value = "clusters", method = RequestMethod.GET)
    List<Category> findClusters(@RequestParam("categoryId") Long categoryId);

    /**
     * 查询分类列表
     * @param category 查询条件
     * @return 分类列表
     */
    @RequestMapping(value = "list", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<Category> list(Category category);

    /**
     * 更新分类下的商品数目
     * @param categoryProductNumMap 商品分类Id和商品数目
     * @return
     */
    @RequestMapping(value = "batUpdateProductNum", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseMessage updateProductNum(Map<Long, Integer> categoryProductNumMap);


    /**
     * 更新分类下的商品数目Axsh
     * @param categoryProductNumMap 商品分类Id和商品数目
     * @return
     */
    @RequestMapping(value = "batUpdateProductNumAxsh", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseMessage updateProductNumAxsh(@RequestParam("shopId") String shopId,Map<Long, Integer> categoryProductNumMap);

}
