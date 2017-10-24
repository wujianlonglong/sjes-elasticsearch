package sjes.elasticsearch.serviceaxsh;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.FacetedPageImpl;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sjes.elasticsearch.common.ResponseMessage;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.domain.AttributeOptionValueModel;
import sjes.elasticsearch.domain.ErpSaleGoodId;
import sjes.elasticsearch.domain.PageModel;
import sjes.elasticsearch.domain.Pageable;
import sjes.elasticsearch.domainaxsh.CategoryIndexAxsh;
import sjes.elasticsearch.domainaxsh.ProductIndexAxsh;
import sjes.elasticsearch.feigns.category.model.*;
import sjes.elasticsearch.feigns.item.model.*;
import sjes.elasticsearch.feigns.sale.feign.ErpSaleFeign;
import sjes.elasticsearch.repository.CategoryRepository;
import sjes.elasticsearch.repositoryaxsh.ProductIndexAxshRepository;
import sjes.elasticsearch.service.AttributeService;
import sjes.elasticsearch.service.CategoryService;
import sjes.elasticsearch.service.StockService;
import sjes.elasticsearch.utils.DateConvertUtils;
import sjes.elasticsearch.utils.LogWriter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.aggregations.AggregationBuilders.filter;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static sjes.elasticsearch.common.SpecificWordHandle.*;
import static sjes.elasticsearch.utils.PinYinUtils.formatAbbrToPinYin;
import static sjes.elasticsearch.utils.PinYinUtils.formatToPinYin;

/**
 * Created by qinhailong on 15-12-2.
 */
@Service("searchAxshService")
public class SearchAxshService {

    private static Logger LOGGER = LoggerFactory.getLogger(SearchAxshService.class);

    @Autowired
    private CategoryRepository categoryRepository;


    @Autowired
    private ProductIndexAxshRepository productIndexAxshRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductAxshService productAxshService;

    @Autowired
    private ProductAttributeValueAxshService productAttributeValueAxshService;

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private ProductIndexAxshService productIndexAxshService;

    @Autowired
    private ProductCategoryAxshService productCategoryAxshService;

    @Autowired
    private BrandAxshService brandAxshService;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private BackupAxshService backupAxshService;

    @Autowired
    private StockService stockService;

    @Autowired
    private ItemPriceAxshService itemPriceAxshService;

    @Autowired
    ErpSaleFeign erpSaleFeign;

    private static final String asxhShopUrl = "http://193.0.1.158:20002/gateShop/getAllShops";

    @Value("${elasticsearchbackup.retry.restore}")
    private int restoreFailRetryTimes;      //恢复失败重试次数

    @Value("${elasticsearchbackup.axshindex}")
    private String AXSH_INDICES;

    //
    private String[] specificChar = {"~", "`", "!", "@", "#", "$", "%", "^", "&", "=", "|", "\\", "{", "}", ";", "\"", "<", ">", "?",
            "～", "｀", "！", "＠", "＃", "￥", "％", "＆", "＝", "｜", "、", "｛", "｝", "；", "“", "《", "》", "？"};

    /**
     * 初始化索引
     */
    public List<CategoryIndexAxsh> initService() throws ServiceException, IOException {
        LogWriter.append("index", "start");
        LOGGER.debug("开始初始化索引！");
        try {
            List<Category> thirdCategories = Lists.newArrayList();
            Map<Long, CategoryIndexAxsh> categoryIndexMap = Maps.newHashMap();
            List<Category> allCategories = categoryService.all();
            Map<Long, Category> categoryIdMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(allCategories) && backupAxshService.isIndexExists()) {
                allCategories.forEach(category -> {
                    Integer grade = category.getGrade();
                    if (null != grade && Constants.CategoryGradeConstants.GRADE_THREE == grade) {
                        thirdCategories.add(category);

                        CategoryIndexAxsh categoryIndex = new CategoryIndexAxsh();
                        categoryIndex.setProductIndexAxshes(Lists.newArrayList());
                        BeanUtils.copyProperties(category, categoryIndex);
                        categoryIndexMap.put(category.getId(), categoryIndex);
                    }
                    categoryIdMap.put(category.getId(), category);
                });
            }
            if (CollectionUtils.isNotEmpty(thirdCategories)) {
                // 分类索引
                categoryRepository.save(thirdCategories);
                LOGGER.debug("分类索引完成......");
                List<Long> categoryIds = Lists.newArrayList(categoryIndexMap.keySet());
                List<ProductImageModel> productImageModels = productAxshService.listByCategoryIds(categoryIds); //耗时操作
                List<AttributeModel> attributeModels = attributeService.lists(categoryIds);
                Map<Long, Attribute> attributeMaps = Maps.newHashMap();
                Map<Long, AttributeOption> attributeOptionMaps = Maps.newHashMap();
                if (CollectionUtils.isNotEmpty(attributeModels)) {
                    attributeModels.forEach(attributeModel -> {
                        attributeMaps.put(attributeModel.getId(), attributeModel);
                        List<AttributeOption> attributeOptions = attributeModel.getAttributeOptions();
                        if (CollectionUtils.isNotEmpty(attributeOptions)) {
                            attributeOptions.forEach(attributeOption ->
                                    attributeOptionMaps.put(attributeOption.getId(), attributeOption));
                        }
                    });
                }
                List<Brand> brands = brandAxshService.listAll();
                Map<Long, String> brandNameMap = Maps.newHashMap();
                if (CollectionUtils.isNotEmpty(brands)) {
                    brands.forEach(brand -> brandNameMap.put(brand.getBrandId(), brand.getName()));
                }
                Map<Long, ProductIndexAxsh> productMap = Maps.newHashMap();
                if (CollectionUtils.isNotEmpty(productImageModels)) {
                    productImageModels.forEach(productImageModel -> {
                        ProductIndexAxsh productIndex = new ProductIndexAxsh();
                        productIndex.setTags(Lists.newArrayList());
                        productIndex.setAttributeOptionValueModels(Lists.newArrayList());
                        productIndex.setProductCategoryIds(Lists.newArrayList());
                        BeanUtils.copyProperties(productImageModel, productIndex);
                        productIndex.setBrandName(brandNameMap.get(productIndex.getBrandId()));
                        Long categoryId = productIndex.getCategoryId();
                        this.populateCategoryTag(categoryIdMap, productIndex, categoryId);
                        categoryIndexMap.get(productImageModel.getCategoryId()).getProductIndexAxshes().add(productIndex);

                        if (StringUtils.isNotBlank(productIndex.getName())) {
                            try {
                                productIndex.setNamePinYin(formatToPinYin(productIndex.getName()).toUpperCase());         //商品名称转拼音
                                productIndex.setNamePinYinAddr(formatAbbrToPinYin(productIndex.getName()).toUpperCase()); //商品名称转拼音首字母
                            } catch (Exception ignored) {
                            }
                        }
                        productIndex.setSearchStr(productIndex.getGoodsId() + "/" + productIndex.getErpGoodsId()
                                + "/" + productIndex.getSn() + "/" + productIndex.getName());

                        productMap.put(productImageModel.getId(), productIndex);
                    });
                }
                List<ProductCategory> productCategories = productCategoryAxshService.listAll();
                if (CollectionUtils.isNotEmpty(productCategories)) {
                    productCategories.forEach(productCategory -> {
                        ProductIndexAxsh productIndex = productMap.get(productCategory.getProductId());
                        Long categoryId = productCategory.getCategoryId();
                        productIndex.getProductCategoryIds().add(categoryId.toString());
                        this.populateCategoryTag(categoryIdMap, productIndex, categoryId);
                    });
                }
                List<ProductAttributeValue> productAttributeValues = productAttributeValueAxshService.listByProductIds(Lists.newArrayList(productMap.keySet()));
                if (CollectionUtils.isNotEmpty(productAttributeValues)) {
                    productAttributeValues.forEach(productAttributeValue -> {
                        ProductIndexAxsh productIndex = productMap.get(productAttributeValue.getProductId());
                        List<Tag> tags = productIndex.getTags();
                        AttributeOptionValueModel attributeOptionValueModel = new AttributeOptionValueModel();
                        Attribute attribute = attributeMaps.get(productAttributeValue.getAttributeId());
                        AttributeOption attributeOption = attributeOptionMaps.get(productAttributeValue.getAttributeOptionId());
                        if (null != attributeOption) {
                            Tag tag = new Tag();
                            tag.setName(attributeOption.getValue());
                            tag.setOrders(tags.size());
                            tags.add(tag);
                        }
                        if (attribute != null) {
                            BeanUtils.copyProperties(attribute, attributeOptionValueModel);
                        }
                        attributeOptionValueModel.setAttributeOption(attributeOption);
                        productIndex.getAttributeOptionValueModels().add(attributeOptionValueModel);
                    });
                }

                // productIndex索引
                List<ProductIndexAxsh> productIndexes = Lists.newArrayList(productMap.values());

                if (CollectionUtils.isNotEmpty(productIndexes)) {
                    Map<Long, ProductIndexAxsh> productIndexMap = Maps.newHashMap();
                    productIndexes.forEach(productIndex -> {
                        productIndexMap.put(productIndex.getErpGoodsId(), productIndex);

                    });
                    List<ItemPrice> itemPrices = itemPriceAxshService.findByErpGoodsIdIn(Lists.newArrayList(productIndexMap.keySet()));
                    if (CollectionUtils.isNotEmpty(itemPrices)) {
                        itemPrices.forEach(itemPrice -> {
                            productIndexMap.get(itemPrice.getErpGoodsId()).getItemPrices().add(itemPrice);
                        });
                    }
                }
                productIndexAxshService.saveBat(productIndexes);        //耗时操作
                LOGGER.debug("商品索引完成......");
                LogWriter.append("index", "success");
            }
            List<CategoryIndexAxsh> categoryIndexList = Lists.newArrayList(categoryIndexMap.values());
            Map<Long, Integer> categoryProductNumMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(categoryIndexList)) {
                categoryIndexList.forEach(categoryIndex -> {
                    categoryProductNumMap.put(categoryIndex.getId(), categoryIndex.getProductIndexAxshes().size());
                });
                ResponseMessage responseMessage = categoryService.updateProductNumAxsh(categoryProductNumMap);
                if (responseMessage.getType().equals(ResponseMessage.Type.success)) {
                    LOGGER.debug("分类绑定的商品数目统计成功！");
                }
            }
            return categoryIndexList;
        } catch (Exception e) {
            LogWriter.append("index", "fail");
            LOGGER.error("初始化索引出现错误！", e);
            throw new ServiceException("初始化索引出现错误！", e.getCause());
        } finally {
            if (!backupAxshService.isIndexVaild()) {
                int retryTimes = restoreFailRetryTimes;
                boolean isRestoreSucceed;

                do {
                    isRestoreSucceed = backupAxshService.restore();
                } while (!isRestoreSucceed && retryTimes-- > 0);
            }
        }
    }

    public List<ProductIndexAxsh> getNewFalgProducts() {
        return productIndexAxshRepository.findByNewFlag(1);
    }

    public void syncNewFalg(List<ProductIndexAxsh> newProducts) {
        if (CollectionUtils.isEmpty(newProducts)) {
            return;
        }
        Map<String, ProductIndexAxsh> newMap = new HashMap<>();
        newProducts.forEach(newp -> newMap.put(newp.getSn(), newp));
        List<String> sns = Lists.newArrayList(newMap.keySet());
        org.springframework.data.domain.Pageable pageable = new PageRequest(0, 999);
        List<ProductIndexAxsh> updateList = new ArrayList<>();
        int length = sns.size();
        int loop = (length + 1000 - 1) / 1000;
        for (int i = 0; i < loop; i++) {
            int start = i * 1000;
            int end = (i + 1) * 1000 >= length ? length : (i + 1) * 1000;
            List<String> subList = sns.subList(start, end);
            List<ProductIndexAxsh> productIndexAxshes = productIndexAxshRepository.findBySnIn(subList, pageable).getContent();
            updateList.addAll(productIndexAxshes);
        }
        if (CollectionUtils.isEmpty(updateList)) {
            return;
        }
        updateList.forEach(update -> {
            update.setNewFlag(1);
            update.setGroundingDate(newMap.get(update.getSn()).getGroundingDate());
        });

        productIndexAxshRepository.save(updateList);
    }


    /**
     * 全量同步当前时间有促销活动的商品
     */
    public void syncPromtionAll() {
        RestTemplate restTemplate = new RestTemplate();
        List<ErpSaleGoodId> erpSaleGoodIds = restTemplate.getForObject("http://srv0.sanjiang.info:20065/anxian/promotions/updateSearch", List.class);
        if (CollectionUtils.isEmpty(erpSaleGoodIds)) {
            return;
        }
        ResponseMessage result = this.indexProductPromotions(erpSaleGoodIds);
    }


    /**
     * 更新商品erp促销信息
     */
    public ResponseMessage updatePromotion() {
        try {
            List<ErpSaleGoodId> erpSaleGoodIds = erpSaleFeign.getErpSaleGoods();//获取商品erp活动
            RestTemplate restTemplate = new RestTemplate();
            String platform = "10005";
            List<String> axshShops = restTemplate.getForObject(asxhShopUrl + "?platform={goodsCode}", List.class, platform);
            //    List<String> axshShops=Arrays.asList(new String[]{"00143","41234","00023","23123"});
            Map<Long, ErpSaleGoodId> goodPromotion = new HashMap<>();
            for (ErpSaleGoodId erpSaleGoodId : erpSaleGoodIds) {
                String promotionType = erpSaleGoodId.getPromotionType();
                switch (promotionType) {
                    case "A":
                        erpSaleGoodId.setPromotionType("金额满减");
                        break;
                    case "D":
                        erpSaleGoodId.setPromotionType("第N件N折");
                        break;
                    case "G":
                        erpSaleGoodId.setPromotionType("数量满减");
                        break;
                    case "K":
                        erpSaleGoodId.setPromotionType("捆绑");
                        break;
                    case "QC":
                        erpSaleGoodId.setPromotionType("全场打折");
                        break;
                    default:
                        erpSaleGoodId.setPromotionType(promotionType);
                        break;

                }
                goodPromotion.put(erpSaleGoodId.getGoodsId(), erpSaleGoodId);
            }

            List<ProductIndexAxsh> productIndexAxshList = IteratorUtils.toList(productIndexAxshRepository.findAll().iterator());
            productIndexAxshList.forEach(productIndexAxsh -> {
                String promotionType = productIndexAxsh.getPromotionType();
                if (StringUtils.isNotEmpty(promotionType)) {
                    //有非erp活动的商品暂时不更新促销类型
                    if (promotionType.equals("秒杀") || promotionType.equals("满赠")) {
                        return;
                    }
                }
                String pro = null;
                String proname = null;
                String shopId = null;
                Long erpgoodsId = productIndexAxsh.getErpGoodsId();
                if (goodPromotion.containsKey(erpgoodsId)) {
                    String shopIds = goodPromotion.get(erpgoodsId).getShopIds();
                    List<String> axshExitShops = new ArrayList<>();
                    for (String axshShop : axshShops) {
                        if (shopIds.contains(axshShop)) {
                            axshExitShops.add(axshShop);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(axshExitShops)) {
                        pro = goodPromotion.get(erpgoodsId).getPromotionType();
                        proname = goodPromotion.get(erpgoodsId).getPromotionName();
                        shopId = StringUtils.join(axshExitShops, ",");
                    }
                }
                productIndexAxsh.setPromotionType(pro);
                productIndexAxsh.setPromotionName(proname);
                productIndexAxsh.setPromotionShop(shopId);

            });


            productIndexAxshRepository.save(productIndexAxshList);
        } catch (Exception ex) {
            LOGGER.error("更新axsh商品erp促销信息失败：" + ex.toString());
            return ResponseMessage.error("更新axsh商品erp促销信息失败：" + ex.toString());
        }
        return ResponseMessage.success("更新axsh商品erp促销信息成功！");
    }


    /**
     * 填充分类标签
     *
     * @param categoryIdMap 分类idMap
     * @param productIndex  商品Index
     * @param categoryId    分类id
     */
    private void populateCategoryTag(Map<Long, Category> categoryIdMap, ProductIndexAxsh productIndex, Long categoryId) {
        List<Tag> tags = productIndex.getTags();
        int tagOrders = tags.size();
        Tag tag;
        if (null != categoryId) {
            do {
                Category category = categoryIdMap.get(categoryId);
                productIndex.getProductCategoryIds().add(categoryId.toString());
                if (null != category) {
                    tag = new Tag();
                    tag.setName(category.getTagName());
                    tag.setOrders(tagOrders + category.getGrade() - 1);
                    tags.add(tag);
                    categoryId = category.getParentId();
                } else {
                    categoryId = null;
                }
            } while (null != categoryId);
        }
    }

    /**
     * 索引单个商品
     *
     * @param productIndex 商品
     */
    public void index(ProductIndexAxsh productIndex) throws ServiceException {
        productIndex.setSales(0L);
        ProductIndexAxsh dbProductIndex = productIndexAxshRepository.findBySn(productIndex.getSn());
        if (null != dbProductIndex) {
            productIndex.setId(dbProductIndex.getId());
            productIndex.setSales(dbProductIndex.getSales());
            productIndex.setPromotionType(dbProductIndex.getPromotionType());
            productIndex.setPromotionName(dbProductIndex.getPromotionName());
            productIndex.setPromotionShop(dbProductIndex.getPromotionShop());
            productIndex.setNewFlag(dbProductIndex.getNewFlag());
            productIndex.setGroundingDate(dbProductIndex.getGroundingDate());
        }
        productIndexAxshRepository.save(productIndex);
    }

    /**
     * 索引productIndex
     *
     * @param productId productIndex
     * @return ProductIndexAxsh
     */
    public void index(Long productId, Integer newFlag) throws ServiceException {
        LOGGER.info(" 商品productId: {}, index beginning ......", new Long[]{productId});
        if (null != productId) {
            ProductIndexAxsh productIndex = buildProductIndex(productAxshService.getProductImageModel(productId));
            if (null != productIndex) {
                productIndex.setSales(0L);
                ProductIndexAxsh dbProductIndex = productIndexAxshRepository.findBySn(productIndex.getSn());
                if (null != dbProductIndex) {
                    productIndex.setId(dbProductIndex.getId());
                    productIndex.setSales(dbProductIndex.getSales());
                    productIndex.setPromotionType(dbProductIndex.getPromotionType());
                    productIndex.setPromotionName(dbProductIndex.getPromotionName());
                    productIndex.setPromotionShop(dbProductIndex.getPromotionShop());
                    productIndex.setNewFlag(dbProductIndex.getNewFlag());
                    productIndex.setGroundingDate(dbProductIndex.getGroundingDate());
                }
                if (newFlag != null && newFlag.equals(1)) {
                    productIndex.setNewFlag(1);
                    productIndex.setGroundingDate(LocalDateTime.now());
                }
                productIndexAxshRepository.save(productIndex);
                LOGGER.info(" 商品productId: {}, index ending ......", new Long[]{productId});
            }
        }
    }

    /**
     * 索引productIndex
     *
     * @param productIds productIndex
     * @param newFlag    是否上架调用接口标志
     * @return ProductIndexAxsh
     */
    @Async
    public void index(List<Long> productIds, Integer newFlag) throws ServiceException {
        String prodIds = StringUtils.join(productIds, ",");
        //LOGGER.info(" 商品productIds: {}, index beginning ......", new String[]{prodIds});
        LOGGER.info(" 商品productIds: , index beginning ......");
        if (CollectionUtils.isNotEmpty(productIds)) {
            List<ProductImageModel> productImageModels = productAxshService.listProductsImageModel(productIds);
            List<ProductIndexAxsh> productIndexes = getProductIndexes(productImageModels, newFlag);
            if (CollectionUtils.isNotEmpty(productIndexes)) {
                productIndexAxshRepository.save(productIndexes);
            }
            // LOGGER.info(" 商品productId: {}, index ending ......", new String[]{prodIds});
            LOGGER.info(" 商品productId: , index ending ......");
        }
    }

    /**
     * 索引productIndex
     *
     * @param sns sns
     * @return ProductIndexAxsh
     */
    public void indexSns(List<String> sns, Integer newFlag) throws ServiceException {
        String snsStr = StringUtils.join(sns, ",");
        LOGGER.info(" sns: {}, index beginning ......", new String[]{snsStr});
        if (CollectionUtils.isNotEmpty(sns)) {
            List<ProductImageModel> productImageModels = productAxshService.listBySns(sns);
            List<ProductIndexAxsh> productIndexes = getProductIndexes(productImageModels, newFlag);
            if (CollectionUtils.isNotEmpty(productIndexes)) {
                productIndexAxshRepository.save(productIndexes);
            }
            LOGGER.info(" 商品sns: {}, index ending ......", new String[]{snsStr});
        }
    }

    private List<ProductIndexAxsh> getProductIndexes(List<ProductImageModel> productImageModels, Integer newFlag) {
        List<ProductIndexAxsh> productIndexes = Lists.newArrayList();
        for (ProductImageModel productImageModel : productImageModels) {
            ProductIndexAxsh productIndex = buildProductIndex(productImageModel);
            if (null != productIndex) {
                productIndex.setSales(0L);
                ProductIndexAxsh dbProductIndex = productIndexAxshRepository.findBySn(productIndex.getSn());
                if (null != dbProductIndex) {
                    productIndex.setId(dbProductIndex.getId());
                    productIndex.setSales(dbProductIndex.getSales());
                    productIndex.setPromotionType(dbProductIndex.getPromotionType());
                    productIndex.setPromotionName(dbProductIndex.getPromotionName());
                    productIndex.setPromotionShop(dbProductIndex.getPromotionShop());
                    productIndex.setNewFlag(dbProductIndex.getNewFlag());
                    productIndex.setGroundingDate(dbProductIndex.getGroundingDate());
                }
                if (newFlag != null && newFlag.equals(1)) {
                    productIndex.setNewFlag(1);
                    productIndex.setGroundingDate(LocalDateTime.now());
                }
                productIndexes.add(productIndex);
            }
        }
        return productIndexes;
    }

    private ProductIndexAxsh buildProductIndex(ProductImageModel productImageModel) {
        Long categoryId = productImageModel.getCategoryId();
        Long productId = productImageModel.getId();
        ProductIndexAxsh productIndexAxsh = null;
        if (null != categoryId) {
            categoryRepository.delete(categoryId);
            productIndexAxsh = new ProductIndexAxsh();
            productIndexAxsh.setAttributeOptionValueModels(Lists.newArrayList());
            BeanUtils.copyProperties(productImageModel, productIndexAxsh);
            List<Tag> tags = Lists.newArrayList();
            List<Category> categories = categoryService.findClusters(categoryId);
            List<String> productCategoryIds = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(categories)) {
                categories.forEach(category -> {
                    Tag tag = new Tag();
                    productCategoryIds.add(category.getId().toString());
                    tag.setName(category.getName());
                    tag.setOrders(tags.size());
                    tags.add(tag);
                    Integer grade = category.getGrade();
                    if (null != grade && Constants.CategoryGradeConstants.GRADE_THREE == grade) {
                        categoryRepository.save(category);
                    }
                });
            }
            Long brandId = productImageModel.getBrandId();
            if (null != brandId) {
                Brand brand = brandAxshService.get(brandId);
                if (null != brand) {
                    productIndexAxsh.setBrandName(brand.getName());
                }
            }
            List<ProductCategory> productCategories = productCategoryAxshService.findProductCategorysByProductId(productId);
            if (CollectionUtils.isNotEmpty(productCategories)) {
                productCategories.forEach(productCategory -> {
                    Long cateId = productCategory.getCategoryId();
                    List<Category> categoryList = categoryService.findClusters(cateId);
                    if (CollectionUtils.isNotEmpty(categoryList)) {
                        categoryList.forEach(category -> {
                            Tag tag = new Tag();
                            tag.setName(category.getTagName());
                            tag.setOrders(tags.size());
                            tags.add(tag);
                        });
                    }
                    productCategoryIds.add(cateId.toString());
                });
            }
            productIndexAxsh.setProductCategoryIds(productCategoryIds);
            List<ProductAttributeValue> productAttributeValues = productAttributeValueAxshService.listByProductIds(Lists.newArrayList(productId));
            List<AttributeModel> attributeModels = attributeService.lists(Lists.newArrayList(categoryId));
            Map<Long, Attribute> attributeMaps = Maps.newHashMap();
            Map<Long, AttributeOption> attributeOptionMaps = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(attributeModels)) {
                attributeModels.forEach(attributeModel -> {
                    attributeMaps.put(attributeModel.getId(), attributeModel);
                    List<AttributeOption> attributeOptions = attributeModel.getAttributeOptions();
                    if (CollectionUtils.isNotEmpty(attributeOptions)) {
                        attributeOptions.forEach(attributeOption -> {
                            attributeOptionMaps.put(attributeOption.getId(), attributeOption);
                        });
                    }
                });
            }
            if (CollectionUtils.isNotEmpty(productAttributeValues)) {
                for (ProductAttributeValue productAttributeValue : productAttributeValues) {
                    AttributeOptionValueModel attributeOptionValueModel = new AttributeOptionValueModel();
                    Attribute attribute = attributeMaps.get(productAttributeValue.getAttributeId());
                    AttributeOption attributeOption = attributeOptionMaps.get(productAttributeValue.getAttributeOptionId());
                    Tag tag = new Tag();
                    tag.setName(attributeOption.getValue());
                    tag.setOrders(tags.size());
                    tags.add(tag);
                    BeanUtils.copyProperties(attribute, attributeOptionValueModel);
                    attributeOptionValueModel.setAttributeOption(attributeOption);
                    productIndexAxsh.getAttributeOptionValueModels().add(attributeOptionValueModel);
                }
            }

            if (StringUtils.isNotBlank(productIndexAxsh.getName())) {
                try {
                    productIndexAxsh.setNamePinYin(formatToPinYin(productIndexAxsh.getName()).toUpperCase());         //商品名称转拼音
                    productIndexAxsh.setNamePinYinAddr(formatAbbrToPinYin(productIndexAxsh.getName()).toUpperCase()); //商品名称转拼音首字母
                } catch (Exception ignored) {
                }
            }
            productIndexAxsh.setSearchStr(productIndexAxsh.getGoodsId() + "/" + productIndexAxsh.getErpGoodsId()
                    + "/" + productIndexAxsh.getSn() + "/" + productIndexAxsh.getName());

            productIndexAxsh.setTags(tags);
            productIndexAxsh.setItemPrices(itemPriceAxshService.findByErpGoodsId(productIndexAxsh.getErpGoodsId()));

        } else {
            //  LOGGER.info(" 商品productId: {}, 分类categoryId为空，索引失败！", new Long[]{productId});
        }
        return productIndexAxsh;
    }

    /**
     * 删除全部索引
     */
    public void deleteIndex() throws ServiceException {
        LogWriter.append("delete", "start");

        if (!backupAxshService.isIndexExists()) {
            LOGGER.error("index missing ......");
            return;
        }
        LOGGER.info("index delete beginning ......");

        LOGGER.info("delete category index beginning ......");
        categoryRepository.deleteAll();
        LOGGER.info("delete category index ending ......");
        LOGGER.info("delete product index beginning ......");
        productIndexAxshRepository.deleteAll();
        LOGGER.info("delete product index ending ......");

        LOGGER.info("index delete successful ......");
        LogWriter.append("delete", "end");
    }

    /**
     * 根据商品id得到ProductIndex
     *
     * @param productId 商品id
     * @return ProductIndexAxsh
     */
    public ProductIndexAxsh getProductIndexByProductId(Long productId) {
        if (null != productId) {
            return productIndexAxshRepository.findOne(productId);
        }
        return null;
    }

    /**
     * 根据商品ERPGOODSID得到ProductIndex
     *
     * @param erpGoodsId ERPGOODSID
     * @return ProductIndexAxsh
     */
    public ProductIndexAxsh getProductIndexByErpGoodsId(Long erpGoodsId) {
        if (null != erpGoodsId) {
            return productIndexAxshRepository.findByErpGoodsId(erpGoodsId);
        }
        return null;
    }


    public List<ProductIndexAxsh> findBySnInAxsh(List<String> sns) {
        org.springframework.data.domain.Pageable pageable = new PageRequest(0, 999);
        Page<ProductIndexAxsh> productIndexAxshPage = productIndexAxshRepository.findBySnIn(sns, pageable);
        return productIndexAxshPage.getContent();
    }

    public List<ProductIndexAxsh> findBySnIn(List<String> sns) {
        org.springframework.data.domain.Pageable pageable = new PageRequest(0, 999);
        Page<ProductIndexAxsh> productIndexPage = productIndexAxshRepository.findBySnIn(sns, pageable);
        return productIndexPage.getContent();
    }


    public List<ProductIndexAxsh> findByErpGoodsIdIn(List<Long> erpGoodsIds) {
        org.springframework.data.domain.Pageable pageable = new PageRequest(0, 999);
        Page<ProductIndexAxsh> productIndexPage = productIndexAxshRepository.findByErpGoodsIdIn(erpGoodsIds, pageable);
        return productIndexPage.getContent();
    }


    public PageModel<ProductIndexAxsh> listProductIndex(LinkedList<Long> erpGoodsIds, Integer page, Integer size) {
        org.springframework.data.domain.Pageable pageable = new PageRequest(page, size);
        if (CollectionUtils.isNotEmpty(erpGoodsIds)) {
            Page<ProductIndexAxsh> productIndexPage = productIndexAxshRepository.findByErpGoodsIdIn(erpGoodsIds, pageable);
            return new PageModel<>(productIndexPage.getContent(), productIndexPage.getTotalElements(), new Pageable(productIndexPage.getNumber(), productIndexPage.getSize()));
        }
        return new PageModel<>(null, 0, new Pageable(1, size));
    }


    public ResponseMessage indexProductPromotions(List<ErpSaleGoodId> erpSaleGoodIds) {
        LOGGER.info("开始更新商品非erp促销类型！");
        try {
            Map<Long, ErpSaleGoodId> productPromotionMap = new HashMap<>();
            erpSaleGoodIds.forEach(erpSaleGoodId -> {
                productPromotionMap.put(erpSaleGoodId.getGoodsId(), erpSaleGoodId);
            });
            List<Long> erpGoodsIds = new ArrayList<>(productPromotionMap.keySet());
            int length = erpGoodsIds.size();
            int batch_num = 1000;
            int loop = (length + batch_num - 1) / batch_num;
            org.springframework.data.domain.Pageable pageable = new PageRequest(0, batch_num);
            List<ProductIndexAxsh> productIndexAxshList = new ArrayList<>();
            for (int i = 0; i < loop; i++) {
                int start = i * batch_num;
                int end = (i + 1) * batch_num >= length ? length : (i + 1) * batch_num;
                List<Long> subList = erpGoodsIds.subList(start, end);
                List<ProductIndexAxsh> productIndexAxshes = productIndexAxshRepository.findByErpGoodsIdIn(subList, pageable).getContent();
                productIndexAxshList.addAll(productIndexAxshes);
            }
            if (CollectionUtils.isNotEmpty(productIndexAxshList)) {
                productIndexAxshList.forEach(productIndexAxsh -> {
                    if (!productPromotionMap.containsKey(productIndexAxsh.getErpGoodsId()))
                        return;
                    ErpSaleGoodId erpSaleGoodId = productPromotionMap.get(productIndexAxsh.getErpGoodsId());
                    productIndexAxsh.setPromotionType(erpSaleGoodId.getPromotionType());
                    productIndexAxsh.setPromotionName(erpSaleGoodId.getPromotionName());
                    productIndexAxsh.setPromotionShop(erpSaleGoodId.getShopIds());
                    productIndexAxsh.setPromotionPrice(erpSaleGoodId.getPromotionPrice() == null ? null : erpSaleGoodId.getPromotionPrice().doubleValue());
                    productIndexAxsh.setSaleHotTips(erpSaleGoodId.getSaleHotTips());
                    productIndexAxsh.setSaleType(erpSaleGoodId.getSaleType());
                });
            }
            productIndexAxshRepository.save(productIndexAxshList);
        } catch (Exception ex) {
            LOGGER.error("更新商品非erp促销类型失败:" + ex.toString());
            return ResponseMessage.error("更新商品非erp促销类型失败:" + ex.toString());
        }
        return ResponseMessage.success("更新商品非erp促销类型成功！");
    }

    /**
     * 商品搜索
     *
     * @param keyword        关键字
     * @param categoryId     分类id
     * @param brandIds       品牌id
     * @param shopId         门店id
     * @param sortType       排序类型
     * @param attributes     属性
     * @param stock          库存
     * @param startPrice     价格satrt
     * @param endPrice       价格 end
     * @param isBargains     是否是惠商品
     * @param page           页面
     * @param size           页面大小
     * @param homeCategoryId 首页分类
     * @return 分页商品信息
     * <p>
     * <p>
     * 搜索逻辑:
     * <p>
     * 1.判断是否搜索关键词是否是拼音 是则与商品名词的拼音进行匹配 2.对特殊的搜索关键词进行处理 3.添加商品名词的查询条件 如果搜索词与商品名词重合85%以上,则这个作为必须条件
     * 4.添加商品品牌的查询条件 如果搜索词中包含品牌名,则只搜索该品牌的商品 5.添加商品分类的查询条件 如果分类名包含搜索词,则只搜索该分类的商品 6.添加商品标签的查询条件
     * 将搜索词与商品标签匹配搜索 如果搜索词有同义词,同时将同义词与标签匹配搜索 7.添加惠商品的过滤条件 8.添加商品状态的过滤条件 9.添加商品分类的过滤条件 10.添加商品品牌的过滤条件
     * 11.添加商品价格的过滤条件 12.添加商品参数的过滤条件 13.添加排序规则 14.限制搜索结果的最低分数线 15.添加搜索关键词高亮 16.聚合搜索结果
     */
    public PageModel productSearch(String keyword, Long categoryId, String brandIds, String shopId, String sortType,
                                   String attributes, Boolean stock, Double startPrice, Double endPrice,
                                   Boolean isBargains, Integer page, Integer size, String promotionName, String homeCategoryId) throws ServiceException {
        Pageable pageable = new Pageable(page, size);

        if ((StringUtils.isBlank(keyword) && null == categoryId && homeCategoryId == null) || (StringUtils.isNotBlank(keyword) && StringUtils.containsAny(keyword, specificChar))) {
            return new PageModel(Lists.newArrayList(), 0, pageable);
        }
        NativeSearchQueryBuilder nativeSearchQueryBuilder;
        BoolQueryBuilder boolQueryBuilder = boolQuery();                 //查询条件
        BoolFilterBuilder boolFilterBuilder = boolFilter();              //过滤条件

        if (StringUtils.isNotBlank(promotionName)) {
            boolQueryBuilder.must(matchQuery("promotionName", promotionName).analyzer("ik"));
            if (StringUtils.isNotBlank(shopId)) {
                boolQueryBuilder.must(wildcardQuery("promotionShop", "*" + shopId + "*"));
            }
        }


        if (StringUtils.isNotBlank(keyword) && keyword.matches("[A-Za-z0-9]+")
                && !specificWords.containsKey(keyword.toUpperCase())) {     //判断搜索关键词是否只有字母数字,且不需要进行特殊处理

            LOGGER.info("~~~~~~~~~~~~~~~~~~~~~ 用户输入的搜索的关键字keyword: {}", keyword);
            //模糊搜索商品名词的拼音
            String keyWordUpperCase = keyword.toUpperCase();
            StringBuffer searchKey = new StringBuffer("");
            if (!StringUtils.startsWith(keyWordUpperCase, "*")) {
                searchKey.append("*");
            }
            searchKey.append(keyWordUpperCase);
            if (!StringUtils.endsWith(keyWordUpperCase, "*")) {
                searchKey.append("*");
            }
            final String searchKeyword = searchKey.toString();
            LOGGER.info("--------------------- 用户输入的搜索的关键字searchKeyword: {}", searchKeyword);
            boolQueryBuilder.should(wildcardQuery("namePinYin", searchKeyword))
                    .should(wildcardQuery("namePinYinAddr", searchKeyword))
                    .minimumNumberShouldMatch(1);

        } else if (StringUtils.isNotBlank(keyword)) {

            LOGGER.info("--------------------- 用户输入的搜索的关键字keyword: {}", keyword);

            final String searchKeyword;                 //搜索的关键词

            if (keyword.matches("[A-Za-z0-9]+") && specificWords.containsKey(keyword.toUpperCase())) {      //对特殊的搜索词（只包含字母数字）进行处理
                searchKeyword = specificWords.get(keyword.toUpperCase());
            } else if (specificWords.containsKey(keyword)) {                                                //对特殊的搜索词进行处理
                searchKeyword = specificWords.get(keyword);
            } else {
                searchKeyword = keyword;
            }
            if (isMatchMostName(searchKeyword)) {
                boolQueryBuilder.must(matchQuery("name", searchKeyword).analyzer("ik").minimumShouldMatch("85%"));      //要求搜索词和商品名的重合度必须在85%以上
            } else {
                if (similarNames.containsKey(searchKeyword)) {

                    //商品名中需要匹配部分搜索词,或者匹配搜索词的同义词
                    boolQueryBuilder.must(boolQuery().should(matchQuery("name", searchKeyword).analyzer("ik"))
                            .should(matchQuery("name", similarNames.get(searchKeyword)).minimumShouldMatch("100%"))
                            .minimumNumberShouldMatch(1));

                } else if (shouldMatchNames.contains(searchKeyword)) {
                    boolQueryBuilder.should(matchQuery("name", searchKeyword).analyzer("ik"));              //商品名可能包含（分词后的）搜索词
                } else {
                    boolQueryBuilder.must(matchQuery("name", searchKeyword).analyzer("ik"));                //商品名必须包含（分词后的）搜索词
                }
            }

//            if (isBandName(searchKeyword)) {
//                if (isspecificBandName(searchKeyword)) {
//                    boolQueryBuilder.should(matchQuery("brandName", searchKeyword).analyzer("ik"));         //（分词后的）搜索词可能包含商品品牌
//                } else {
//                    boolQueryBuilder.must(matchQuery("brandName", searchKeyword).analyzer("ik"));           //（分词后的）搜索词必须包含商品品牌
//                }
//            }

            if (isBandName(searchKeyword)) {
                boolQueryBuilder.should(matchQuery("brandName", searchKeyword).analyzer("ik"));         //（分词后的）搜索词可能包含商品品牌
            }

            if (specificCategories.containsKey(searchKeyword)) {

                //指定搜索结果必须为某些分类下的商品
                BoolQueryBuilder categoryQueryBuilder = boolQuery();
                specificCategories.get(searchKeyword).forEach(specificCategoryId ->
                        categoryQueryBuilder.should(termQuery("productCategoryIds", specificCategoryId)));
                boolQueryBuilder.must(categoryQueryBuilder.minimumNumberShouldMatch(1)).boost(2.0f);

            } else if (isCategoryName(searchKeyword)) {

                //搜索结果的分类名中必须包含搜索词
                BoolQueryBuilder categoryQueryBuilder = boolQuery();
                StringBuffer likeName = new StringBuffer("");
                String cateSearchKey = searchKeyword.replaceAll(" ", "");
                if (!StringUtils.startsWith(cateSearchKey, "*")) {
                    likeName.append("*");
                }
                likeName.append(cateSearchKey);
                if (!StringUtils.endsWith(cateSearchKey, "*")) {
                    likeName.append("*");
                }
                categoryRepository.findByNameLike(likeName.toString()).forEach(category ->
                        categoryQueryBuilder.should(termQuery("productCategoryIds", category.getId())));
                boolQueryBuilder.must(categoryQueryBuilder.minimumNumberShouldMatch(1)).boost(2.0f);
            }

            boolQueryBuilder.should(nestedQuery("tags", matchQuery("tags.name", searchKeyword).analyzer("ik")));          //将搜索词与标签匹配查询

            //将搜索词的同义词与标签匹配查询
            if (similarTags.containsKey(searchKeyword)) {
                boolQueryBuilder.should(nestedQuery("tags", matchQuery("tags.name", similarTags.get(searchKeyword)).analyzer("ik")));
            }
        } else {
            boolQueryBuilder.should(matchAllQuery());
        }

//        if (StringUtils.isNotBlank(placeNames)) {     //限定产地
//            String[] placeNameArr = StringUtils.split(placeNames, "_");
//            if (placeNameArr.length > 0) {
//                BoolQueryBuilder palceNamesBoolQueryBuilder = boolQuery();
//                for (String placeName : placeNameArr) {
//                    palceNamesBoolQueryBuilder.should(wildcardQuery("place", "*" + placeName + "*"));
//                }
//                boolQueryBuilder.must(palceNamesBoolQueryBuilder);
//            }
//        }

//        nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);


        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(boolQueryBuilder)
                .add(ScoreFunctionBuilders.scriptFunction("return 2*((doc[\'sales\'].value+1)/(doc[\'sales\'].value+2));", "groovy"))
                .add(FilterBuilders.termFilter("newFlag", "1"), ScoreFunctionBuilders.weightFactorFunction(3))
                .add(FilterBuilders.existsFilter("promotionName"), ScoreFunctionBuilders.weightFactorFunction(5))
                .scoreMode("sum");
        nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(functionScoreQueryBuilder);


        //判断是否过滤惠商品
        if (isBargains != null && isBargains) {
            boolFilterBuilder.must(termFilter("isBargains", isBargains));
        }

        //限制搜索结果的商品状态为0
        boolFilterBuilder.must(termFilter("status", 0));

        //判断是否限定搜索结果的分类
        if (null != categoryId) {
            boolFilterBuilder.must(termFilter("productCategoryIds", categoryId));
        }
        if (null != homeCategoryId) {
            boolFilterBuilder.must(termFilter("homeCategoryIds", homeCategoryId));
        }

        //去除搜索结果中指定分类的商品
        if (StringUtils.isNotBlank(keyword) && exceptCategories.containsKey(keyword)) {
            exceptCategories.get(keyword).forEach(exceptCategoryId ->
                    boolFilterBuilder.mustNot(termFilter("productCategoryIds", exceptCategoryId)));
        }

        //限定搜索结果中的商品品牌
        if (StringUtils.isNotBlank(brandIds)) {
            String[] brandIdArr = StringUtils.split(brandIds, "_");
            if (brandIdArr.length > 0) {
                BoolFilterBuilder brandIdsBoolFilterBuilder = boolFilter();
                for (String brandId : brandIdArr) {
                    brandIdsBoolFilterBuilder.should(termFilter("brandId", brandId));
                }
                boolFilterBuilder.must(brandIdsBoolFilterBuilder);
            }
        }
        //过滤掉没有查询商场的价格的商品
        if (null != shopId) {
            boolFilterBuilder.must(nestedFilter("itemPrices", boolFilter().must(termFilter("itemPrices.shopId", shopId))));
        }
        if ((null != startPrice || null != endPrice) && null != shopId) {
            //   boolFilterBuilder.must(nestedFilter("itemPrices", boolFilter().must(termFilter("itemPrices.shopId", shopId))));
            if (null != startPrice && null != endPrice) {
                boolFilterBuilder.must(nestedFilter("itemPrices", boolFilter().must(rangeFilter("itemPrices.memberPrice").gt(startPrice).lt(endPrice))));
            } else if (null != startPrice) {  //限定最低价格
                boolFilterBuilder.must(nestedFilter("itemPrices", boolFilter().must(rangeFilter("itemPrices.memberPrice").gt(startPrice))));
            } else if (null != endPrice) { //限定最高价格
                boolFilterBuilder.must(nestedFilter("itemPrices", boolFilter().must(rangeFilter("itemPrices.memberPrice").lt(endPrice))));
            }
        }


//        if (null != startPrice && null != endPrice) {
//            boolFilterBuilder.must(rangeFilter("memberPrice").gt(startPrice).lt(endPrice));
//        }
//        else if (null != startPrice) {  //限定最低价格
//            boolFilterBuilder.must(rangeFilter("memberPrice").gt(startPrice));
//        }
//        else if (null != endPrice) { //限定最高价格
//            boolFilterBuilder.must(rangeFilter("memberPrice").lt(endPrice));
//        }

        //限定商品参数
        if (StringUtils.isNotBlank(attributes)) {
            String[] attrs = StringUtils.split(attributes, "_");
            if (attrs.length > 0) {
                for (String attr : attrs) {
                    String[] attrValues = StringUtils.split(attr, "-");
                    if (attrValues.length == 2) {
                        String attributeId = attrValues[0];
                        String attributeOptionId = attrValues[1];

                        boolFilterBuilder.must(nestedFilter("attributeOptionValueModels",
                                nestedFilter("attributeOptionValueModels.attributeOption",
                                        boolFilter().must(termFilter("attributeOptionValueModels.attributeOption.attributeId", Long.valueOf(attributeId)))
                                                .must(termFilter("attributeOptionValueModels.attributeOption.id", Long.valueOf(attributeOptionId))))));
                    }
                }
            }
        }

        nativeSearchQueryBuilder.withFilter(boolFilterBuilder);

        //对搜索结果进行排序
        if (null != sortType && !sortType.equals("default")) {
            SortBuilder sortBuilder = null;
            if (sortType.equals("sales")) {  //销量降序
                sortBuilder = SortBuilders.fieldSort("sales").order(SortOrder.DESC);
            } else if (sortType.equals("salesUp")) {  //销量升序
                sortBuilder = SortBuilders.fieldSort("sales").order(SortOrder.ASC);
            } else if (sortType.equals("price")) {  //价格降序
                sortBuilder = SortBuilders.fieldSort("itemPrices.memberPrice").setNestedPath("itemPrices").setNestedFilter(termFilter("itemPrices.shopId", shopId)).order(SortOrder.DESC);
                //  sortBuilder = SortBuilders.fieldSort("memberPrice").order(SortOrder.DESC);
            } else if (sortType.equals("priceUp")) {  //销价格升序
                sortBuilder = SortBuilders.fieldSort("itemPrices.memberPrice").setNestedPath("itemPrices").setNestedFilter(termFilter("itemPrices.shopId", shopId)).order(SortOrder.ASC);
                // sortBuilder = SortBuilders.fieldSort("memberPrice").order(SortOrder.ASC);
            }
            nativeSearchQueryBuilder.withSort(sortBuilder);
        }

        //获取第一个结果的分类号，并提高该分类商品的排名
//        elasticsearchTemplate.query(nativeSearchQueryBuilder.withPageable(new PageRequest(0, 1)).withMinScore(1f).withIndices("axsh").withTypes("products").build(), searchResponse -> {
//            if (searchResponse.getHits().getTotalHits() > 0) {
//                SearchHit[] searchHits = searchResponse.getHits().getHits();
//                nativeSearchQueryBuilder.withSort(SortBuilders.scriptSort("_score + (doc['categoryId'].value == myVal ? 1 : 0) * 2", "number").param("myVal", searchHits[0].getSource().get("categoryId")).order(SortOrder.DESC));  //使用Groovy脚本自定义排序
//            }
//            return null;
//        });

        //限制搜索结果的最低分数线
        if (null == categoryId && homeCategoryId == null) {
            if (StringUtils.isNotBlank(keyword) && keyword.matches("[A-Za-z0-9]+")) {
                nativeSearchQueryBuilder.withMinScore(0.1f);
            } else {
                nativeSearchQueryBuilder.withMinScore(0.2f);
            }
        }

        //搜索结果中关键词的高亮显示
        if (StringUtils.isNotBlank(keyword)) {
            nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("name").highlightQuery(matchQuery("name", keyword)).preTags("<b class=\"highlight\">").postTags("</b>"));
        }


//        final long[] totalHits = {0};   //总记录数
        Set<Long> categoryIdSet = Sets.newHashSet();
        Gson gson = new Gson();
        FacetedPage<ProductIndexAxsh> queryForPage = elasticsearchTemplate.queryForPage(
                nativeSearchQueryBuilder.withPageable(new PageRequest(0, 999)).withIndices(AXSH_INDICES).withTypes("products")
                        .addAggregation(filter("aggs").filter(boolFilterBuilder).subAggregation(terms("categoryIdSet").field("categoryId").size(100)))
                        .build(), ProductIndexAxsh.class, new SearchResultMapper() {

                    @Override
                    public <T> FacetedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, org.springframework.data.domain.Pageable pageable) {
                        List<ProductIndexAxsh> productIndexes = new ArrayList<>();

//                        totalHits[0] = searchResponse.getHits().getTotalHits();

                        //聚合结果中所有的分类Id
                        Filter aggs = searchResponse.getAggregations().get("aggs");
                        Terms categoryIdAggr = aggs.getAggregations().get("categoryIdSet");
                        categoryIdAggr.getBuckets().forEach(bucket -> categoryIdSet.add(bucket.getKeyAsNumber().longValue()));

                        if (searchResponse.getHits().getTotalHits() > 0) {
                            //final int[] i = {1};
                            searchResponse.getHits().forEach(searchHit -> {
                                ProductIndexAxsh productIndex = null;
                                try {
                                    productIndex = (ProductIndexAxsh) mapToObject(ProductIndexAxsh.class, searchHit.getSource());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                                HighlightField highlightNameField = highlightFields.get("name");
                                if (productIndex != null) {
                                    if (highlightNameField != null && highlightNameField.fragments() != null) {
                                        productIndex.setDisplayName(highlightNameField.fragments()[0].string());
                                    } else {
                                        productIndex.setDisplayName(productIndex.getName());
                                    }
                                    productIndexes.add(productIndex);
                                }
                            });
                        }

                        return new FacetedPageImpl<>((List<T>) productIndexes);
                    }
                });
        List<ProductIndexAxsh> content = queryForPage.getContent();
        List<ProductIndexAxsh> returnContent = Lists.newArrayList();
        int addCount = 0;
        if (CollectionUtils.isNotEmpty(content)) {
            Map<Long, ProductIndexAxsh> productIndexMap = Maps.newHashMap();
            content.forEach(productIndex -> {
                categoryIdSet.add(productIndex.getCategoryId());
                productIndexMap.put(productIndex.getErpGoodsId(), productIndex);
            });
            Map<Long, Integer> stockMap = stockService.stockForList(shopId, Lists.newArrayList(productIndexMap.keySet()));
            if (null == size) {
                size = pageable.getSize();
            }
            int startIndex = pageable.getPage() * size;
            int endIndex = (pageable.getPage() + 1) * size;
            int contentSize = content.size();
            if (endIndex > contentSize) {
                endIndex = contentSize;
            }
            for (int i = 0; i < contentSize; i++) {
                ProductIndexAxsh productIndex = content.get(i);
                Integer stockNum = stockMap.get(productIndex.getErpGoodsId());
                long stockNumber = null != stockNum ? stockNum : 0;
                //库存不为0，门店价格不为空
                if (stockNumber > 0) {
                    List<ItemPrice> itemPrices = productIndex.getItemPrices();
                    if (CollectionUtils.isNotEmpty(itemPrices)) {
                        int itemPriceSize = itemPrices.size();
                        for (int j = 0; j < itemPriceSize; j++) {
                            ItemPrice itemPrice = gson.fromJson(gson.toJson(itemPrices.get(j)), ItemPrice.class);
                            if (StringUtils.equals(itemPrice.getShopId(), shopId)) {
                                if (addCount >= startIndex && addCount < endIndex) {
                                    productIndex.setSalePrice(itemPrice.getSalePrice());
                                    productIndex.setMemberPrice(itemPrice.getMemberPrice());
                                    returnContent.add(productIndex);
                                }
                                addCount++;
                                break;
                            }
                        }
                    }

                }


//                if (stockNumber > 0) {
//                    if (addCount >= startIndex && addCount < endIndex) {
//                        List<ItemPrice> itemPrices = productIndex.getItemPrices();
//                        if (CollectionUtils.isNotEmpty(itemPrices)) {
//                            int itemPriceSize = itemPrices.size();
//                            for (int j = 0; j < itemPriceSize; j++) {
//                                ItemPrice itemPrice = gson.fromJson(gson.toJson(itemPrices.get(j)), ItemPrice.class);
//                                if (StringUtils.equals(itemPrice.getShopId(), shopId)) {
//                                    productIndex.setSalePrice(itemPrice.getSalePrice());
//                                    productIndex.setMemberPrice(itemPrice.getMemberPrice());
//
//                                    break;
//                                }
//                            }
//                        }
//                        returnContent.add(productIndex);
//                    }
//                    addCount++;
//                }
            }
        }

        //对搜索结果重新排序，防止门店获取价格时排序错误
//        if (null != sortType && !sortType.equals("default")) {
//            if (sortType.equals("sales")) {  //销量降序
//                returnContent.sort((h1, h2) -> h2.getSales().compareTo(h1.getSales()));
//            } else if (sortType.equals("salesUp")) {  //销量升序
//                returnContent.sort((h1, h2) -> h1.getSales().compareTo(h1.getSales()));
//            } else if (sortType.equals("price")) {  //价格降序
//                returnContent.sort((h1, h2) -> h2.getMemberPrice().compareTo(h1.getMemberPrice()));
//            } else if (sortType.equals("priceUp")) {  //销价格升序
//                returnContent.sort((h1, h2) -> h1.getMemberPrice().compareTo(h2.getMemberPrice()));
//            }
//        }
        return new PageModel(returnContent, addCount, categoryIdSet, pageable);
    }

    /**
     * 查询分类
     *
     * @param keyword    关键字
     * @param categoryId 分类 id
     * @param page       页面
     * @param size       页面大小
     * @return 分类信息
     */
    public PageModel<Category> categorySearch(String keyword, Long categoryId, Integer page, Integer size) throws ServiceException {
        Pageable pageable = new Pageable(page, size);
        if (StringUtils.isBlank(keyword) && null == categoryId) {
            return new PageModel(Lists.newArrayList(), 0, pageable);
        }
        NativeSearchQueryBuilder nativeSearchQueryBuilder;
        boolean filterFlag = false; //判断是否需要过滤的标记

        nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(matchAllQuery());

        BoolFilterBuilder boolFilterBuilder = boolFilter();

        if (StringUtils.isNotBlank(keyword)) {
            boolFilterBuilder.must(termFilter("name", keyword));
            filterFlag = true;
        }

        if (null != categoryId) {
            boolFilterBuilder.must(termFilter("id", categoryId));
            filterFlag = true;
        }

        if (filterFlag) {
            nativeSearchQueryBuilder.withFilter(boolFilterBuilder);
        }

        FacetedPage<Category> facetedPage = categoryRepository.search(nativeSearchQueryBuilder.withPageable(new PageRequest(pageable.getPage(), pageable.getSize())).build());
        return new PageModel(facetedPage.getContent(), facetedPage.getTotalElements(), pageable);
    }

    /**
     * Map转Object
     *
     * @param classType 类
     * @param map       Map
     * @return 对象
     */
    private Object mapToObject(Class classType, Map map) throws IllegalAccessException,
            InstantiationException, IntrospectionException, InvocationTargetException {

        BeanInfo beanInfo = Introspector.getBeanInfo(classType); // 获取类属性
        Object obj = classType.newInstance(); // 创建 JavaBean 对象

        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();
            String type = descriptor.getPropertyType().getTypeName();

            if (map.containsKey(propertyName)) {
                Object value = map.get(propertyName);
                if (value != null) {
                    if (type.endsWith("Long")) {
                        descriptor.getWriteMethod().invoke(obj, Long.valueOf(value.toString()));
                    } else if (type.endsWith("BigDecimal")) {
                        descriptor.getWriteMethod().invoke(obj, new BigDecimal(value.toString()));
                    } else if (type.endsWith("ProductImage")) {
                        descriptor.getWriteMethod().invoke(obj, mapToObject(descriptor.getPropertyType(), (HashMap) value));
                    } else if (type.endsWith("LocalDateTime")) {
                        descriptor.getWriteMethod().invoke(obj, DateConvertUtils.asLocalDateTime(new Date(Long.parseLong(value.toString()))));
                    } else {
                        descriptor.getWriteMethod().invoke(obj, value);
                    }
                }
            }
        }
        return obj;
    }

    /**
     * 判断关键词是否为品牌
     */
    private boolean isBandName(String keyword) {
        return elasticsearchTemplate.query(
                new NativeSearchQueryBuilder().withQuery(matchQuery("brandName", keyword).analyzer("ik")).withMinScore(0.01f)
                        .withPageable(new PageRequest(0, 1)).withIndices("axsh").withTypes("products").build(),
                searchBrandNameResponse -> searchBrandNameResponse.getHits().getTotalHits() > 0);
    }

    /**
     * 判断是否是特殊品牌名,比如小浣熊（统一小浣熊方便面,小浣熊倍润润唇膏）
     */
    private boolean isspecificBandName(String keyword) {
        final boolean[] flag = {false};
        specificBrandNames.forEach(specificBrandName -> {
            if (specificBrandName.contains(keyword) || keyword.contains(specificBrandName)) {
                flag[0] = true;
                return;
            }
        });
        return flag[0];
    }

    /**
     * 判断搜索关键词是否是分类
     */
    private boolean isCategoryName(String keyword) {
        return categoryRepository.countByNameLike("*" + keyword.replaceAll(" ", "") + "*") > 0;
    }

    /**
     * 判断是否匹配大部分商品名（搜索搜索关键词和商品名分词后进行匹配,85%以上的词重合）
     */
    private boolean isMatchMostName(String keyword) {
        return keyword.length() > 3 && elasticsearchTemplate.query(
                new NativeSearchQueryBuilder().withQuery(matchQuery("name", keyword).analyzer("ik").minimumShouldMatch("85%"))
                        .withPageable(new PageRequest(0, 1)).withIndices("axsh").withTypes("products").build(),
                searchNameResponse -> searchNameResponse.getHits().getTotalHits() > 0);
    }


    /**
     * 更新是否为新品标志（上架两周内的为新品）--安鲜生活
     */
    public void updateNewFlagAxsh() {
        try {
            List<ProductIndexAxsh> productIndexAxshes = productIndexAxshRepository.findByNewFlag(1);
            if (CollectionUtils.isEmpty(productIndexAxshes)) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            List<ProductIndexAxsh> updateList = new ArrayList<>();
            productIndexAxshes.forEach(productIndexAxsh -> {
                LocalDateTime groundingDate = productIndexAxsh.getGroundingDate();
                long daydiff = ChronoUnit.DAYS.between(groundingDate, now);
                if (daydiff > 15) {
                    productIndexAxsh.setNewFlag(0);
                    updateList.add(productIndexAxsh);
                }
            });

            if (CollectionUtils.isNotEmpty(updateList)) {
                productIndexAxshRepository.save(updateList);
            }
        } catch (Exception ex) {
            LOGGER.error("更新是否为新品标志（上架两周内的为新品）--安鲜生活失败：" + ex.toString());
        }
    }


}
