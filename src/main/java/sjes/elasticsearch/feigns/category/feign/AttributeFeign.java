package sjes.elasticsearch.feigns.category.feign;


import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.feigns.category.model.AttributeModel;
import sjes.elasticsearch.feigns.constants.CategoryConstant;

import java.util.List;


/**
 * Created by mac on 15/8/27.
 */
@FeignClient(CategoryConstant.SJES_API_CATEGORY)
@RequestMapping("attributes")
public interface AttributeFeign {

    /**
     * 根据分类ids查询AttributeModel列表
     * @param categoryIds 分类ids
     * @return AttributeModel列表
     */
    @RequestMapping(value = "categoryIds", method = RequestMethod.POST)
    List<AttributeModel> lists(List<Long> categoryIds);

}
