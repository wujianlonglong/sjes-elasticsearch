package sjes.elasticsearch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.service.SearchLogService;

@RestController
@RequestMapping("logs")
public class SearchLogController {

    @Autowired
    private SearchLogService searchLogService;

    /**
     * 索引搜索记录
     *
     * @param keyword 搜索的关键字
     * @param categoryId 分类id
     * @param shopId 商店id
     * @param sortType 排序类型
     * @param startPrice 起始价格
     * @param endPrice 最高价格
     * @param userAgent 用户代理
     * @param ip IP地址
     * @throws ServiceException
     */
    @RequestMapping(method = RequestMethod.PUT)
    public void index(String keyword, Long categoryId, String shopId, String sortType, Double startPrice, Double endPrice, String userAgent, String ip) throws ServiceException {
        searchLogService.index(keyword, categoryId, shopId, sortType, startPrice, endPrice, userAgent, ip);
    }
}
