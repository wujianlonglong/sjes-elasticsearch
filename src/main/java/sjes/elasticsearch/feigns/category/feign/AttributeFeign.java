package sjes.elasticsearch.feigns.category.feign;


import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.category.model.Attribute;
import sjes.elasticsearch.feigns.category.model.AttributeModel;

import java.util.List;


/**
 * Created by qinhailong on 15/8/27.
 */
@FeignClient(Constants.SJES_API_CATEGORY)
@RequestMapping("attributes")
public interface AttributeFeign {

    /**
     * 根据主键查询指定的属性信息
     * @param id 主键id
     * @return 属性信息
     */
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    Attribute findById(@PathVariable("id") Long id);

    /**
     * 根据分类ids查询AttributeModel列表
     * @param categoryIds 分类ids
     * @return AttributeModel列表
     */
    @RequestMapping(value = "categoryIds", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<AttributeModel> lists(List<Long> categoryIds);

}
