package sjes.elasticsearch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.CategoryIndex;
import sjes.elasticsearch.domain.PageModel;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.feigns.category.model.Category;
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

    /**
     * 建立索引
     * @return 索引的数据
     */
    @RequestMapping(value = "index", method = RequestMethod.GET)
    public List<CategoryIndex> index() throws ServiceException {
        searchService.deleteIndex();
         return searchService.initService();
    }

    /**
     * 索引productIndex
     * @param productIndex productIndex
     * @return ProductIndex
     */
    @RequestMapping(method = RequestMethod.POST)
    public void index(@RequestBody ProductIndex productIndex) throws ServiceException {
        searchService.index(productIndex);
    }

    /**
     * 删除索引
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public void deleteIndex() throws ServiceException  {
        searchService.deleteIndex();
    }

    /**
     * 查询商品列表
     * @param keyword 关键字
     * @param categoryId 分类id
     * @param brandIds 品牌ids
     * @param placeNames 地区
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
    public PageModel<ProductIndex> search(String keyword, Long categoryId, String brandIds, String placeNames, String shopId, String sortType, String attributes, Boolean stock, Double startPrice, Double endPrice, Integer page, Integer size) throws ServiceException {
        return searchService.productSearch(keyword, categoryId, brandIds, placeNames, shopId, sortType, attributes, stock, startPrice, endPrice, page, size);
    }

    /**
     * 查询分类列表
     * @param keyword 关键字
     * @param categoryId 分类 id
     * @param page
     * @param size
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "categorySearch",method = RequestMethod.GET)
    public PageModel<Category> categorySearch(String keyword, Long categoryId, Integer page, Integer size) throws ServiceException {
        return searchService.categorySearch(keyword, categoryId, page, size);
    }

    /**
     * 根据商品id得到ProductIndex
     * @param productId 分类id
     * @return ProductIndex
     */
    @RequestMapping(value = "{productId}", method = RequestMethod.GET)
    public ProductIndex getProductIndexByProductId(@PathVariable("productId") Long productId) {
        return searchService.getProductIndexByProductId(productId);
    }

}
