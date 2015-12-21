package sjes.elasticsearch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.HotWordModel;
import sjes.elasticsearch.service.SearchLogService;

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
     *
     * @return 索引的数据
     */
    @RequestMapping(method = RequestMethod.PUT)
    public void index(String keyword, Long categoryId, String shopId, String sortType, Double startPrice, Double endPrice, String userAgent, String ip) throws ServiceException {
        searchLogService.index(keyword, categoryId, shopId, sortType, startPrice, endPrice, userAgent, ip);
    }


    /**
     * 获取热门搜索词
     *
     * @param maxCount 热门搜索词的最大数量，默认为5
     * @return 热门搜索词
     */
    @RequestMapping(value = "hotwords", method = RequestMethod.GET)
    public List<HotWordModel> getHotWords(Integer maxCount) throws ServiceException {
        return searchLogService.getHotWords(maxCount);
    }
}
