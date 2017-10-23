package sjes.elasticsearch.controlleraxsh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.HotWordModel;
import sjes.elasticsearch.serviceaxsh.SearchLogAxshService;

import java.util.List;

@RestController
@RequestMapping("logsaxsh")
public class SearchLogAxshController {

    @Autowired
    private SearchLogAxshService searchLogAxshService;

    /**
     * 删除所有搜索记录
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public void deleteAll() {
        searchLogAxshService.deleteAll();
    }

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
        searchLogAxshService.index(keyword, categoryId, shopId, sortType, startPrice, endPrice, userAgent, ip);
    }

    /**
     * 获取热门搜索词
     *
     * @param maxCount 热门搜索词的最大数量，默认为5
     * @return 热门搜索词
     */
    @RequestMapping(value = "hotwords", method = RequestMethod.GET)
    public List<HotWordModel> getHotWords(Integer maxCount) throws ServiceException {
        return searchLogAxshService.getHotWords(maxCount);
    }
}
