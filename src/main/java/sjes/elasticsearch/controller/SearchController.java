package sjes.elasticsearch.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import sjes.elasticsearch.common.ResponseMessage;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.*;
import sjes.elasticsearch.feigns.category.model.Category;
import sjes.elasticsearch.opt.ProductSalesOpt;
import sjes.elasticsearch.service.BackupService;
import sjes.elasticsearch.service.SearchLogService;
import sjes.elasticsearch.service.SearchService;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by qinhailong on 15-12-2.
 */
@RestController
@RequestMapping("searchs")
public class SearchController {

    private static final Logger LOG = LoggerFactory.getLogger(SearchController.class);
    @Autowired
    private SearchService searchService;

    @Autowired
    private SearchLogService searchLogService;

    @Autowired
    private BackupService backupService;

    @Value("${elasticsearchbackup.retry.backup}")
    private int backupFailRetryTimes;       //备份失败重试次数


    @Autowired
    ProductSalesOpt productSalesOpt;
    /**
     * 建立索引
     *
     * @return 索引的数据
     */
    @RequestMapping(value = "index", method = RequestMethod.GET)
    public List<CategoryIndex> index() throws ServiceException, IOException {
        int retryTimes = backupFailRetryTimes;
        boolean isBackupSucceed;

        do {
            isBackupSucceed = backupService.backup();
        } while (!isBackupSucceed && retryTimes-- > 0);

        searchService.deleteIndex();
        List<CategoryIndex> categoryIndexList= searchService.initService();
      //  searchAxshService.updatePromotion();//更新商品erp促销信息
        productSalesOpt.productSalesAllSync();//全量同步商品销售量
        return categoryIndexList;
    }

    /**
     * 索引productIndex
     *
     * @param productIndex productIndex
     */
    @RequestMapping(method = RequestMethod.POST)
    public void index(@RequestBody ProductIndex productIndex) throws ServiceException {
        searchService.index(productIndex);
    }

    /**
     * 索引productIndex
     *
     * @param productId productIndex
     */
    @RequestMapping(method = RequestMethod.PUT)
    public void index(@RequestParam("productId") Long productId) throws ServiceException {
        searchService.index(productId);
    }

    /**
     * 索引productIndex
     */
    @RequestMapping(value = "index/productIds", method = RequestMethod.PUT)
    public void index(@RequestBody List<Long> productIds) throws ServiceException {
        searchService.index(productIds);
    }

    /**
     * 索引productIndex
     *
     * @param sns sns
     * @return ProductIndexAxsh
     */
    @RequestMapping(value = "index/sns", method = RequestMethod.PUT)
    public void indexSns(@RequestBody List<String> sns) throws ServiceException {
        searchService.indexSns(sns);
    }

    /**
     * 删除索引
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public void deleteIndex() throws ServiceException {
        searchService.deleteIndex();
    }

    /**
     * 查询商品列表
     *
     * @param keyword    关键字
     * @param categoryId 分类id
     * @param brandIds   品牌ids
     * @param shopId     门店id
     * @param sortType   排序类型
     * @param attributes 属性
     * @param stock      库存
     * @param startPrice 价格satrt
     * @param endPrice   价格 end
     * @param page       页面
     * @param size       页面大小
     * @return 分页商品信息
     */
    @RequestMapping(method = RequestMethod.GET)
    public PageModel<ProductIndex> search(String keyword, Long categoryId, String brandIds, String shopId, String sortType, String attributes, Boolean stock, Double startPrice, Double endPrice, Boolean isBargains, Integer page, Integer size) throws ServiceException {
        if (StringUtils.isNotBlank(keyword)) {
            keyword = keyword.trim();
        }
        searchLogService.index(keyword, categoryId, shopId, sortType, startPrice, endPrice, null, null);//添加搜索记录
        return searchService.productSearch(keyword, categoryId, brandIds, shopId, sortType, attributes, stock, startPrice, endPrice, isBargains, page, size);
    }

    /**
     * 查询分类列表
     *
     * @param keyword    关键字
     * @param categoryId 分类 id
     * @param page       页码
     * @param size       每页数量
     */
    @RequestMapping(value = "categorySearch", method = RequestMethod.GET)
    public PageModel<Category> categorySearch(String keyword, Long categoryId, Integer page, Integer size) throws ServiceException {
        if (StringUtils.isNotBlank(keyword)) {
            keyword = keyword.trim();
        }
        return searchService.categorySearch(keyword, categoryId, page, size);
    }

    /**
     * 根据商品id得到ProductIndex
     *
     * @param productId 分类id
     * @return ProductIndexAxsh
     */
    @RequestMapping(value = "{productId}", method = RequestMethod.GET)
    public ProductIndex getProductIndexByProductId(@PathVariable("productId") Long productId) {
        return searchService.getProductIndexByProductId(productId);
    }

    /**
     * 根据ERPID得到ProductIndex
     */
    @RequestMapping(value = "/erpGoodsId/{erpGoodsId}", method = RequestMethod.GET)
    public ProductIndex getProductIndexByErpGoodsId(@PathVariable("erpGoodsId") Long erpGoodsId) {
        return searchService.getProductIndexByErpGoodsId(erpGoodsId);
    }

    @RequestMapping(value = "/erpGoodsId/list", method = RequestMethod.POST)
    public PageModel<ProductIndex> listProductIndexByErpGoodsIds(@RequestBody SearchParam<LinkedList<Long>> searchParam) {
        if (null == searchParam) {
            return new PageModel<>(null, 0, new Pageable(1, 10));
        }
        LinkedList<Long> erpGoodsIds = searchParam.getData();
        Integer page = searchParam.getPage();
        Integer size = searchParam.getSize();
        return searchService.listProductIndex(erpGoodsIds, page, size);
    }

    /**
     * 更新商品非erp促销类型
     *
     * @param erpSaleGoodIds
     */
    @RequestMapping(value = "index/productPromotions", method = RequestMethod.POST)
    public ResponseMessage updatePromotionType(@RequestBody List<ErpSaleGoodId> erpSaleGoodIds) {
        if (CollectionUtils.isEmpty(erpSaleGoodIds)) {
            return ResponseMessage.error("请求参数为空！");
        }
        ResponseMessage responseMessage = searchService.indexProductPromotions(erpSaleGoodIds);
        return responseMessage;
    }
}
