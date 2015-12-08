package sjes.elasticsearch.feigns.category.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.category.model.Tag;

import java.util.List;

/**
 * Created by qinhailong on 15-12-8.
 */
@FeignClient(Constants.SJES_API_CATEGORY)
@RequestMapping("tags")
public interface TagFeign {

    /**
     * 查询所有的标签信息
     * @return 所有的标签信息
     */
    @RequestMapping(method = RequestMethod.GET)
    List<Tag> all();
}
