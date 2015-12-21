package sjes.elasticsearch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.CategoryIndex;
import sjes.elasticsearch.domain.PageModel;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.feigns.category.model.Category;
import sjes.elasticsearch.service.SearchLogService;
import sjes.elasticsearch.service.SearchService;

import java.util.List;

/**
 * Created by qinhailong on 15-12-2.
 */
@RestController
@RequestMapping("logs")
public class SearchLogController {

    @Autowired
    private SearchLogService searchLogService;

    /**
     * 建立索引
     * @return 索引的数据
     */
    @RequestMapping(method = RequestMethod.PUT)
    public void index(@RequestParam("keyword") String keyword) throws ServiceException {
        searchLogService.index(keyword);
    }
}
