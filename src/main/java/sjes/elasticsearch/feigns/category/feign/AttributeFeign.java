package sjes.elasticsearch.feigns.category.feign;


import org.springframework.cloud.netflix.feign.FeignClient;

import org.springframework.web.bind.annotation.RequestMapping;
import sjes.elasticsearch.feigns.constants.CategoryConstant;


/**
 * Created by mac on 15/8/27.
 */
@FeignClient(CategoryConstant.SJES_API_CATEGORY)
@RequestMapping("attributes")
public interface AttributeFeign {

}
