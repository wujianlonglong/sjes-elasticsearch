package sjes.elasticsearch.feigns.category.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.category.model.CategoryBasicAttributesModel;

import java.util.List;

/**
 * Created by qinhailong on 15-12-7.
 */
@FeignClient(Constants.SJES_API_CATEGORY)
@RequestMapping("categoryAttributes")
public interface CategoryBasicAttributesFeign {

    /**
     * 新增或修改分类查询属性列表
     * @param categoryBasicAttributesModelList 分类查询属性列表
     * @return 是否成功
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    void saveOrUpdate(List<CategoryBasicAttributesModel> categoryBasicAttributesModelList);
}
