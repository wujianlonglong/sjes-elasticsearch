package sjes.elasticsearch.feigns.item.feignaxsh;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.item.model.Brand;

import java.util.List;

/**
 * Created by qinhailong on 15-12-25.
 */
@FeignClient(value = Constants.AXSH_API_ITEM)
@RequestMapping(value = "brands/anxian")
public interface BrandAxshFeign {

    /**
     * 查询所有品牌信息
     * @return 品牌列表
     */
    @RequestMapping(method = RequestMethod.GET)
    List<Brand> listAll();

    /**
     * 查询所有品牌信息
     * @return 品牌列表
     */
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    Brand get(@PathVariable("id") Long id);

}
