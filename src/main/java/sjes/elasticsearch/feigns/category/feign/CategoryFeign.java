package sjes.elasticsearch.feigns.category.feign;


import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.category.model.Category;

import java.util.List;

/**
 * Created by qinhailong on 15/8/26.
 */
@FeignClient(Constants.SJES_API_CATEGORY)
@RequestMapping("categorys")
public interface CategoryFeign {

    /**
     * 根据分类id查询分类簇对象
     *
     * @param categoryId 　分类id
     * @return　分类簇对象
     */
    @RequestMapping(value = "clusters", method = RequestMethod.GET)
    List<Category> findClusters(Long categoryId);

    /**
     * 查询分类列表
     * @param category 查询条件
     * @return 分类列表
     */
    @RequestMapping(value = "list", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<Category> list(Category category);

}
