package sjes.elasticsearch.feigns.category.feign;


import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.category.model.Category;

import java.util.List;

/**
 * Created by mac on 15/8/26.
 */
@FeignClient(Constants.SJES_API_CATEGORY)
@RequestMapping("categorys")
public interface CategoryFeign {

    /**
     * 根据分类级别查询分类列表信息
     * @param grade 分类级别
     * @return 分类列表信息
     */
    @RequestMapping(value = "grade", method = RequestMethod.GET)
    List<Category> findByGrade(@RequestParam("grade") Integer grade);

}
