package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.FacetedPageImpl;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.domain.*;
import sjes.elasticsearch.feigns.category.model.*;
import sjes.elasticsearch.feigns.item.model.Brand;
import sjes.elasticsearch.feigns.item.model.ProductAttributeValue;
import sjes.elasticsearch.feigns.item.model.ProductCategory;
import sjes.elasticsearch.feigns.item.model.ProductImageModel;
import sjes.elasticsearch.repository.CategoryRepository;
import sjes.elasticsearch.repository.ProductIndexRepository;
import sjes.elasticsearch.utils.DateConvertUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;

import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by qinhailong on 15-12-2.
 */
@Service("searchService")
public class SearchService {

    private static Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductIndexRepository productIndexRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductAttributeValueService productAttributeValueService;

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private ProductIndexService productIndexService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private BackupService backupService;

    @Value("${elasticsearch-backup.retry.restore}")
    private int restoreFailRetryTimes;      //恢复失败重试次数

    /**
     * 初始化索引
     */
    public List<CategoryIndex> initService() throws ServiceException, IOException {
        LOGGER.debug("开始初始化索引！");
        try {
            List<Category> thirdCategories = Lists.newArrayList();
            Map<Long, CategoryIndex> categoryIndexMap = Maps.newHashMap();
            List<Category> allCategories = categoryService.all();
            Map<Long, Category> categoryIdMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(allCategories) && backupService.isIndexExists()) {
                allCategories.forEach(category -> {
                    Integer grade = category.getGrade();
                    if (null != grade && Constants.CategoryGradeConstants.GRADE_THREE == grade) {
                        thirdCategories.add(category);

                        CategoryIndex categoryIndex = new CategoryIndex();
                        categoryIndex.setProductIndexes(Lists.newArrayList());
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
                List<ProductImageModel> productImageModels = productService.listByCategoryIds(categoryIds); //耗时操作
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
                List<Brand> brands = brandService.listAll();
                Map<Long, String> brandNameMap = Maps.newHashMap();
                if (CollectionUtils.isNotEmpty(brands)) {
                    brands.forEach(brand-> brandNameMap.put(brand.getBrandId(), brand.getName()));
                }
                Map<Long, ProductIndex> productMap = Maps.newHashMap();
                if (CollectionUtils.isNotEmpty(productImageModels)) {
                    productImageModels.forEach(productImageModel -> {
                        ProductIndex productIndex = new ProductIndex();
                        productIndex.setTags(Lists.newArrayList());
                        productIndex.setAttributeOptionValueModels(Lists.newArrayList());
                        productIndex.setProductCategoryIds(Lists.newArrayList());
                        BeanUtils.copyProperties(productImageModel, productIndex);
                        productIndex.setBrandName(brandNameMap.get(productIndex.getBrandId()));
                        Long categoryId = productIndex.getCategoryId();
                        this.populateCategoryTag(categoryIdMap, productIndex, categoryId);
                        categoryIndexMap.get(productImageModel.getCategoryId()).getProductIndexes().add(productIndex);
                        productMap.put(productImageModel.getId(), productIndex);
                    });
                }
                List<ProductCategory> productCategories = productCategoryService.listAll();
                if (CollectionUtils.isNotEmpty(productCategories)) {
                    productCategories.forEach(productCategory -> {
                        ProductIndex productIndex = productMap.get(productCategory.getProductId());
                        Long categoryId = productCategory.getCategoryId();
                        productIndex.getProductCategoryIds().add(categoryId.toString());
                        this.populateCategoryTag(categoryIdMap, productIndex, categoryId);
                    });
                }
                List<ProductAttributeValue> productAttributeValues = productAttributeValueService.listByProductIds(Lists.newArrayList(productMap.keySet()));
                if (CollectionUtils.isNotEmpty(productAttributeValues)) {
                    productAttributeValues.forEach(productAttributeValue -> {
                        ProductIndex productIndex = productMap.get(productAttributeValue.getProductId());
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
                List<ProductIndex> productIndexes = Lists.newArrayList(productMap.values());
                productIndexService.saveBat(productIndexes);        //耗时操作
                LOGGER.debug("商品索引完成......");
            }
            return Lists.newArrayList(categoryIndexMap.values());
        } catch (Exception e) {
            LOGGER.error("初始化索引出现错误！", e);
            throw new ServiceException("初始化索引出现错误！", e.getCause());
        } finally {
            if(!backupService.isIndexVaild()){
                int retryTimes = restoreFailRetryTimes;
                boolean isRestoreSucceed;

                do {
                    isRestoreSucceed = backupService.restore();
                }while (!isRestoreSucceed && retryTimes-- > 0);
            }
            LOGGER.info("index finish");
        }
    }

    /**
     * 填充分类标签
     * @param categoryIdMap 分类idMap
     * @param productIndex 商品Index
     * @param categoryId 分类id
     */
    private void populateCategoryTag(Map<Long, Category> categoryIdMap, ProductIndex productIndex, Long categoryId) {
        List<Tag> tags = productIndex.getTags();
        int tagOrders = tags.size();
        Tag tag ;
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
                }
                else {
                    categoryId = null;
                }
            } while(null != categoryId);
        }
    }

    /**
     * 索引单个商品
     * @param productIndex 商品
     * @throws ServiceException
     */
    public void index(ProductIndex productIndex) throws ServiceException {
        productIndexRepository.save(productIndex);
    }

    /**
     * 索引productIndex
     * @param productId productIndex
     * @return ProductIndex
     */
    @RequestMapping(method = RequestMethod.PUT)
    public void index(Long productId) throws ServiceException {
        LOGGER.info(" 商品productId: {}, index beginning ......", new Long[] { productId });
        if (null != productId) {
            categoryRepository.delete(productId);
            ProductImageModel productImageModel = productService.getProductImageModel(productId);
            ProductIndex productIndex = new ProductIndex();
            productIndex.setAttributeOptionValueModels(Lists.newArrayList());
            org.springframework.beans.BeanUtils.copyProperties(productImageModel, productIndex);
            Long categoryId = productIndex.getCategoryId();
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
                });
            }
            Long brandId = productImageModel.getBrandId();
            if (null != brandId) {
                Brand brand = brandService.get(brandId);
                if (null != brand) {
                    productIndex.setBrandName(brand.getName());
                }
            }
            List<ProductCategory> productCategories = productCategoryService.findProductCategorysByProductId(productId);
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
            productIndex.setProductCategoryIds(productCategoryIds);
            List<ProductAttributeValue> productAttributeValues = productAttributeValueService.listByProductIds(Lists.newArrayList(productId));
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
                productAttributeValues.forEach(productAttributeValue -> {
                    AttributeOptionValueModel attributeOptionValueModel = new AttributeOptionValueModel();
                    Attribute attribute = attributeMaps.get(productAttributeValue.getAttributeId());
                    AttributeOption attributeOption = attributeOptionMaps.get(productAttributeValue.getAttributeOptionId());
                    Tag tag = new Tag();
                    tag.setName(attributeOption.getValue());
                    tag.setOrders(tags.size());
                    tags.add(tag);
                    org.springframework.beans.BeanUtils.copyProperties(attribute, attributeOptionValueModel);
                    attributeOptionValueModel.setAttributeOption(attributeOption);
                    productIndex.getAttributeOptionValueModels().add(attributeOptionValueModel);
                });
            }
            productIndex.setTags(tags);
            productIndexRepository.save(productIndex);
            LOGGER.info(" 商品productId: {}, index ending ......", new Long[] { productId });
        }
    }

    /**
     * 删除全部索引
     *
     * @throws ServiceException
     */
    public void deleteIndex() throws ServiceException {

        if (!backupService.isIndexExists()) {
            LOGGER.error("index missing ......");
            return;
        }
        LOGGER.info("index delete beginning ......");

        LOGGER.info("delete category index beginning ......");
        categoryRepository.deleteAll();
        LOGGER.info("delete category index ending ......");
        LOGGER.info("delete product index beginning ......");
        productIndexRepository.deleteAll();
        LOGGER.info("delete product index ending ......");

        LOGGER.info("index delete successful ......");
    }

    /**
     * 根据商品id得到ProductIndex
     * @param productId 商品id
     * @return ProductIndex
     */
    public ProductIndex getProductIndexByProductId(Long productId) {
        if (null != productId) {
            return productIndexRepository.findOne(productId);
        }
        return null;
    }

    /**
     * 商品搜索
     *
     * @param keyword    关键字
     * @param categoryId 分类id
     * @param brandIds   品牌id
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
    public PageModel productSearch(String keyword, Long categoryId, String brandIds, String shopId, String sortType, String attributes, Boolean stock, Double startPrice, Double endPrice, Integer page, Integer size) throws ServiceException {
        Pageable pageable = new Pageable(page, size);

        if (StringUtils.isBlank(keyword) && null == categoryId) {
            return new PageModel(Lists.newArrayList(), 0, pageable);
        }

        if (StringUtils.isNotBlank(keyword) && null == categoryId) {
            List<Category> categoryList = categoryRepository.findByName(keyword);
            if (categoryList != null && categoryList.size() > 0){
                if(categoryList.size() > 1){
                    int max = -1;
                    for (Category category : categoryList) {
                        int count = productIndexRepository.findByCategoryId(category.getId()).size();
                        if (count > max) {
                            max = count;
                            categoryId = category.getId();
                        }
                    }
                }else{
                    categoryId = categoryList.get(0).getId();
                }
            }
        }

        NativeSearchQueryBuilder nativeSearchQueryBuilder;
        BoolQueryBuilder boolQueryBuilder = boolQuery();
        final boolean[] brandMatchFlag = {false};   //判断名牌名称是否匹配到
        final boolean[] nameAllMatchFlag = {false};    //判断名称是否全部匹配到

        if (StringUtils.isNotBlank(keyword)) {
            boolQueryBuilder.must(matchQuery("name", keyword).analyzer("ik"));                //根据商品名称分词检索
            boolQueryBuilder.should(nestedQuery("tags", matchQuery("tags.name", keyword).analyzer("ik")));  //根据标签分词检索

            final Long tempCategoryId = categoryId;

            //先判断输入的关键字是否为品牌，是则作为必须条件
            elasticsearchTemplate.query(
                    new NativeSearchQueryBuilder().withQuery(boolQuery().must(boolQuery().should(matchQuery("brandName", keyword).analyzer("ik")).should(wildcardQuery("brandName","*"+keyword+"*")).minimumNumberShouldMatch(1))).withMinScore(0.01f).withPageable(new PageRequest(0, 1)).withIndices("sjes").withTypes("products").build(),
                    searchBrandNameResponse -> {
                        //LOGGER.info(searchBrandNameResponse.getHits().getMaxScore()+"");
                        if (searchBrandNameResponse.getHits().getTotalHits() > 0) {
                            boolQueryBuilder.must(boolQuery().should(matchQuery("brandName", keyword).analyzer("ik")).should(wildcardQuery("brandName","*"+keyword+"*")).minimumNumberShouldMatch(1));           //根据商品品牌名称搜索

                            if(tempCategoryId != null) {
                                //判断搜索词是否全部匹配到，是则作为必须条件
                                elasticsearchTemplate.query(
                                        new NativeSearchQueryBuilder().withQuery(
                                                boolQuery().must(matchQuery("name", keyword).analyzer("ik").minimumShouldMatch("100%"))
                                                        .must(matchQuery("brandName", keyword).analyzer("ik"))).withPageable(new PageRequest(0, 1)).withIndices("sjes").withTypes("products").build(),
                                        searchNameResponse -> {
                                            if (searchNameResponse.getHits().getTotalHits() > 0) {
                                                boolQueryBuilder.must(matchQuery("name", keyword).analyzer("ik").minimumShouldMatch("100%"));           //完全匹配商品名称
                                                nameAllMatchFlag[0] = true;
                                            }
                                            return null;
                                        });
                            }
                            brandMatchFlag[0] = true;
                        }
                        return null;
                    });

            if(null == categoryId && !nameAllMatchFlag[0]) {
                boolQueryBuilder.should(matchQuery("name", keyword).analyzer("ik").minimumShouldMatch("80%"));                //根据商品名称分词检索

                //匹配分类名来获取最有可能的分类
                elasticsearchTemplate.query(new NativeSearchQueryBuilder().withQuery(matchQuery("tagName", keyword).minimumShouldMatch("50%").analyzer("ik")).withPageable(new PageRequest(0, 2)).withMinScore(1f).withIndices("sjes").withTypes("categories").build(), searchResponse -> {
                    if (searchResponse.getHits().getTotalHits() > 0) {
                        SearchHit[] searchHits = searchResponse.getHits().getHits();
                        for (int i = 0; i < searchHits.length; i++) {
                            boolQueryBuilder.should(termQuery("categoryId", searchHits[i].getSource().get("id")));  //根据分类查询
                        }
                    }
                    return null;
                });
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
        nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);

        BoolFilterBuilder boolFilterBuilder = boolFilter();
        boolFilterBuilder.must(termFilter("status", 0));

        if (null != categoryId) {       //限定商品分类
            BoolFilterBuilder categoryIdBoolFilterBuilder = boolFilter().should(termFilter("categoryId", categoryId));
            List<Category> categories = categorySearch(categoryId);
            if (null != categories) {
                categories.forEach(category -> categoryIdBoolFilterBuilder.should(termFilter("categoryId", category.getId())));
            }
            boolFilterBuilder.must(categoryIdBoolFilterBuilder);
        }

        if (StringUtils.isNotBlank(brandIds)) {     //限定品牌
            String[] brandIdArr = StringUtils.split(brandIds, "_");
            if (brandIdArr.length > 0) {
                BoolFilterBuilder brandIdsBoolFilterBuilder = boolFilter();
                for (String brandId : brandIdArr) {
                    brandIdsBoolFilterBuilder.should(termFilter("brandId", brandId));
                }
                boolFilterBuilder.must(brandIdsBoolFilterBuilder);
            }
        }

        if (null != startPrice) {    //限定最低价格
            boolFilterBuilder.must(rangeFilter("memberPrice").gt(startPrice));
        }

        if (null != endPrice) {      //限定最高价格
            boolFilterBuilder.must(rangeFilter("memberPrice").lt(endPrice));
        }

        if (StringUtils.isNotBlank(attributes)) {  //限定商品参数
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

        if (null != sortType && !sortType.equals("default")) {       //排序
            SortBuilder sortBuilder = null;
            if (sortType.equals("sales")) {  //销量降序
                sortBuilder = SortBuilders.fieldSort("sales").order(SortOrder.DESC);
            } else if (sortType.equals("salesUp")) {  //销量升序
                sortBuilder = SortBuilders.fieldSort("sales").order(SortOrder.ASC);
            } else if (sortType.equals("price")) {  //价格降序
                sortBuilder = SortBuilders.fieldSort("memberPrice").order(SortOrder.DESC);
            } else if (sortType.equals("priceUp")) {  //销价格升序
                sortBuilder = SortBuilders.fieldSort("memberPrice").order(SortOrder.ASC);
            }
            nativeSearchQueryBuilder.withSort(sortBuilder);
        } else if(!brandMatchFlag[0] && null == categoryId){
            //获取第一个结果的分类号，并提高该分类商品的排名
            elasticsearchTemplate.query(nativeSearchQueryBuilder.withPageable(new PageRequest(0, 1)).withMinScore(1f).withIndices("sjes").withTypes("products").build(), searchResponse -> {
                if (searchResponse.getHits().getTotalHits() > 0) {
                    SearchHit[] searchHits = searchResponse.getHits().getHits();
                    nativeSearchQueryBuilder.withSort(SortBuilders.scriptSort("_score + (doc['categoryId'].value == myVal ? 1 : 0) * 2", "number").param("myVal", searchHits[0].getSource().get("categoryId")).order(SortOrder.DESC));  //使用Groovy脚本自定义排序
                }
                return null;
            });
        }

        if(null == categoryId){
            nativeSearchQueryBuilder.withMinScore(0.618f);
        }else{
            nativeSearchQueryBuilder.withMinScore(0.1f);
        }

        final long[] totalHits = {0};   //总记录数
        FacetedPage<ProductIndex> queryForPage = elasticsearchTemplate.queryForPage(
                nativeSearchQueryBuilder.withPageable(new PageRequest(pageable.getPage(), pageable.getSize())).withIndices("sjes").withTypes("products")
                        .withHighlightFields(new HighlightBuilder.Field("name").preTags("<b class=\"highlight\">").postTags("</b>")).build(), ProductIndex.class, new SearchResultMapper() {

            @Override
            public <T> FacetedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, org.springframework.data.domain.Pageable pageable) {
                List<ProductIndex> productIndexes = new ArrayList<>();

                totalHits[0] = searchResponse.getHits().getTotalHits();
                if (searchResponse.getHits().getTotalHits() > 0) {

                    searchResponse.getHits().forEach(searchHit -> {

                        ProductIndex productIndex = null;
                        try {
                            productIndex = (ProductIndex) mapToObject(aClass, searchHit.getSource());
                        } catch (IllegalAccessException | InstantiationException | IntrospectionException | InvocationTargetException e) {
                            e.printStackTrace();
                        }

                        Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                        HighlightField highlightNameField = highlightFields.get("name");
                        if (highlightNameField != null && highlightNameField.fragments() != null && productIndex != null) {
                            productIndex.setDisplayName(highlightNameField.fragments()[0].string());
                        }else{
                            productIndex.setDisplayName(productIndex.getName());
                        }
                        productIndexes.add(productIndex);
                    });
                }

                return new FacetedPageImpl<>((List<T>) productIndexes);
            }
        });

        return new PageModel(queryForPage.getContent(), totalHits[0], pageable);

//        FacetedPage<ProductIndex> facetedPage = productIndexRepository.search(nativeSearchQueryBuilder.withPageable(new PageRequest(pageable.getPage(), pageable.getSize())).withMinScore(0.35f).build());
//        return new PageModel(facetedPage.getContent(), facetedPage.getTotalElements(), pageable);
    }

    /**
     * 查询分类
     *
     * @param keyword    关键字
     * @param categoryId 分类 id
     * @param page       页面
     * @param size       页面大小
     * @return 分类信息
     * @throws ServiceException
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
     * 搜索分类下所有的子分类
     *
     * @param categoryId 分类编号
     * @return 子分类列表
     * @throws ServiceException
     */
    public List<Category> categorySearch(Long categoryId) throws ServiceException {
        if (null == categoryId) {
            return null;
        }

        NativeSearchQueryBuilder nativeSearchQueryBuilder;
        nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(wildcardQuery("treePath", "*," + categoryId + ",*"));
        SearchQuery searchQuery = nativeSearchQueryBuilder.build();
        List<Category> categories = categoryRepository.search(searchQuery).getContent();

        return categories;
    }

    /**
     * Map转Object
     *
     * @param classType 类
     * @param map Map
     * @return 对象
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IntrospectionException
     * @throws InvocationTargetException
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
}
