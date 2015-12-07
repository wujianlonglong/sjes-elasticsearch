package sjes.elasticsearch.feigns.category.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.category.model.AttributeOption;


/**
 * Created by qinhailong on 15-11-16.
 */
@FeignClient(Constants.SJES_API_CATEGORY)
@RequestMapping("attributeOptions")
public interface AttributeOptionFeign {

    /**
     * 根据主键得到指定的属性项
     *
     * @param id 主键
     * @return 属性项
     */
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    AttributeOption findOne(@PathVariable("id") Long id);
}
