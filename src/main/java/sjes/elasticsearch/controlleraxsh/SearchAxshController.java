package sjes.elasticsearch.controlleraxsh;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import sjes.elasticsearch.common.ResponseMessage;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.controller.SearchParam;
import sjes.elasticsearch.domain.ErpSaleGoodId;
import sjes.elasticsearch.domain.PageModel;
import sjes.elasticsearch.domain.Pageable;
import sjes.elasticsearch.domainaxsh.CategoryIndexAxsh;
import sjes.elasticsearch.domainaxsh.ProductIndexAxsh;
import sjes.elasticsearch.feigns.category.model.Category;
import sjes.elasticsearch.opt.ProductSalesOpt;
import sjes.elasticsearch.serviceaxsh.BackupAxshService;
import sjes.elasticsearch.serviceaxsh.ProductAxshService;
import sjes.elasticsearch.serviceaxsh.SearchAxshService;
import sjes.elasticsearch.serviceaxsh.SearchLogAxshService;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by qinhailong on 15-12-2.
 */
@RestController
@RequestMapping("searchsaxsh")
public class SearchAxshController {

    private static final Logger LOG = LoggerFactory.getLogger(SearchAxshController.class);
    @Autowired
    private SearchAxshService searchAxshService;

    @Autowired
    private SearchLogAxshService searchLogAxshService;

    @Autowired
    private BackupAxshService backupAxshService;

    @Autowired
    ProductSalesOpt productSalesOpt;

    @Autowired
    ProductAxshService productAxshService;

    @Value("${elasticsearchbackup.retry.backup}")
    private int backupFailRetryTimes;       //备份失败重试次数

    /**
     * 建立索引
     *
     * @return 索引的数据
     */
    @RequestMapping(value = "index", method = RequestMethod.GET)
    public List<CategoryIndexAxsh> index() throws ServiceException, IOException {
        int retryTimes = backupFailRetryTimes;
        boolean isBackupSucceed;

        do {
            isBackupSucceed = backupAxshService.backup();
        } while (!isBackupSucceed && retryTimes-- > 0);


        List<ProductIndexAxsh> newProducts=searchAxshService.getNewFalgProducts();
        searchAxshService.deleteIndex();
        List<CategoryIndexAxsh> categoryIndexAxshes = searchAxshService.initService();
        searchAxshService.syncNewFalg(newProducts);//全量同步新品标记
        searchAxshService.syncPromtionAll();//全量同步促销活动
        productSalesOpt.productSalesAllSync();//全量同步商品销售量
        productAxshService.allHomeCategorySync();//全量同步首页分类-商品关系数据
        return categoryIndexAxshes;
    }

    /**
     * 索引productIndex
     *
     * @param productIndex productIndex
     */
    @RequestMapping(method = RequestMethod.POST)
    public void index(@RequestBody ProductIndexAxsh productIndex) throws ServiceException {
        searchAxshService.index(productIndex);
    }

    /**
     * 索引productIndex
     *
     * @param productId productIndex
     * @param newFlag 是否上架调用接口标志
     */
    @RequestMapping(method = RequestMethod.PUT)
    public void index(@RequestParam("productId") Long productId,@RequestParam("newFlag") Integer newFlag) throws ServiceException {
        searchAxshService.index(productId,newFlag);
    }


    /**
     * 索引productIndex
     * @param newFlag 是否上架调用接口标志
     */
    @RequestMapping(value = "index/productIds", method = RequestMethod.PUT)
    public void index(@RequestBody List<Long> productIds, Integer newFlag) throws ServiceException {
        searchAxshService.index(productIds,newFlag);
    }

    /**
     * 索引productIndex
     *
     * @param sns sns
     * @return ProductIndexAxsh
     */
    @RequestMapping(value = "index/sns", method = RequestMethod.PUT)
    public void indexSns(@RequestBody List<String> sns,Integer newFlag) throws ServiceException {
        searchAxshService.indexSns(sns,newFlag);
    }

    /**
     * 删除索引
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public void deleteIndex() throws ServiceException {
        searchAxshService.deleteIndex();
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
     * @param promotionName 促销名称
     * @param homeCategoryId 首页分类
     * @return 分页商品信息
     */
    @RequestMapping(method = RequestMethod.GET)
    public PageModel<ProductIndexAxsh> search(String keyword, Long categoryId, String brandIds, String shopId, String sortType, String attributes, Boolean stock, Double startPrice, Double endPrice, Boolean isBargains, Integer page, Integer size, String promotionName,String homeCategoryId) throws ServiceException {
        if (StringUtils.isNotBlank(keyword)) {
            keyword = keyword.trim();
        }
        searchLogAxshService.index(keyword, categoryId, shopId, sortType, startPrice, endPrice, null, null);//添加搜索记录
        return searchAxshService.productSearch(keyword, categoryId, brandIds, shopId, sortType, attributes, stock, startPrice, endPrice, isBargains, page, size, promotionName,homeCategoryId);
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
        return searchAxshService.categorySearch(keyword, categoryId, page, size);
    }

    /**
     * 根据商品id得到ProductIndex
     *
     * @param productId 分类id
     * @return ProductIndexAxsh
     */
    @RequestMapping(value = "{productId}", method = RequestMethod.GET)
    public ProductIndexAxsh getProductIndexByProductId(@PathVariable("productId") Long productId) {
        return searchAxshService.getProductIndexByProductId(productId);
    }

    /**
     * 根据ERPID得到ProductIndex
     */
    @RequestMapping(value = "/erpGoodsId/{erpGoodsId}", method = RequestMethod.GET)
    public ProductIndexAxsh getProductIndexByErpGoodsId(@PathVariable("erpGoodsId") Long erpGoodsId) {
        return searchAxshService.getProductIndexByErpGoodsId(erpGoodsId);
    }

    @RequestMapping(value = "/erpGoodsId/list", method = RequestMethod.POST)
    public PageModel<ProductIndexAxsh> listProductIndexByErpGoodsIds(@RequestBody SearchParam<LinkedList<Long>> searchParam) {
        if (null == searchParam) {
            return new PageModel<>(null, 0, new Pageable(1, 10));
        }
        LinkedList<Long> erpGoodsIds = searchParam.getData();
        Integer page = searchParam.getPage();
        Integer size = searchParam.getSize();
        return searchAxshService.listProductIndex(erpGoodsIds, page, size);
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
        ResponseMessage responseMessage = searchAxshService.indexProductPromotions(erpSaleGoodIds);
        return responseMessage;
    }


    @RequestMapping(value="syncPromotionAll",method=RequestMethod.GET)
    public void syncPromotionAll(){
        searchAxshService.syncPromtionAll();
    }


    /**
     * 全量同步首页商品分类数据
     */
    @RequestMapping(value="/allHomeCategorySync")
    public void allHomeCategorySync(){
        productAxshService.allHomeCategorySync();
    }


}
