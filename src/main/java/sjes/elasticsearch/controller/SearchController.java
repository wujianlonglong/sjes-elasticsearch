package sjes.elasticsearch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.CategoryIndex;
import sjes.elasticsearch.domain.PageModel;
import sjes.elasticsearch.feigns.item.model.ProductImageModel;
import sjes.elasticsearch.service.SearchService;

import java.util.List;

/**
 * Created by qinhailong on 15-12-2.
 */
@RestController
@RequestMapping("searchs")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @RequestMapping(value = "index", method = RequestMethod.GET)
    public List<CategoryIndex> index() throws ServiceException {
        return searchService.initService();
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public String deleteIndex() {
        try {
            searchService.deleteIndex();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return "delete";
    }

    /**
     * 查询分类商品列表
     * @param keyword 关键字
     * @param categoryId 分类id
     * @param brandId 品牌id
     * @param brandName 品牌名称
     * @param shopId 门店id
     * @param sortType 排序类型
     * @param attributes 属性
     * @param stock 库存
     * @param startPrice 价格satrt
     * @param endPrice 价格 end
     * @param page 页面
     * @param size 页面大小
     * @return 分页商品信息
     */
    @RequestMapping(method = RequestMethod.GET)
    public PageModel<ProductImageModel> categorySearch(String keyword, Long categoryId, Long brandId, String brandName, String shopId, String sortType, String attributes, Boolean stock, Double startPrice, Double endPrice, Integer page, Integer size) throws ServiceException {
        return searchService.search(keyword, categoryId, brandId, brandName, shopId, sortType, attributes, stock, startPrice, endPrice, page, size);
    }

}
