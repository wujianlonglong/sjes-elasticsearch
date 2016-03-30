package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.FacetedPageImpl;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
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
import sjes.elasticsearch.utils.LogWriter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;

import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static sjes.elasticsearch.common.SpecificWordHandle.*;
import static sjes.elasticsearch.utils.PinYinUtils.formatAbbrToPinYin;
import static sjes.elasticsearch.utils.PinYinUtils.formatToPinYin;

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
    public List<CategoryIndex> initService() throws ServiceException {
        LogWriter.append("index", "start");
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

                        if (StringUtils.isNotBlank(productIndex.getName())) {
                            try {
                                productIndex.setNamePinYin(formatToPinYin(productIndex.getName()).toUpperCase());         //商品名称转拼音
                                productIndex.setNamePinYinAddr(formatAbbrToPinYin(productIndex.getName()).toUpperCase()); //商品名称转拼音首字母
                            } catch (Exception ignored) {}
                        }
                        productIndex.setSearchStr(productIndex.getGoodsId() + "/" + productIndex.getErpGoodsId()
                                + "/" + productIndex.getSn() + "/" + productIndex.getName());

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
                LogWriter.append("index", "success");
            }
            return Lists.newArrayList(categoryIndexMap.values());
        } catch (Exception e) {
            LogWriter.append("index", "fail");
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
    public void index(Long productId) throws ServiceException {
        LOGGER.info(" 商品productId: {}, index beginning ......", new Long[] { productId });
        if (null != productId) {
            ProductIndex productIndex = buildProductIndex(productService.getProductImageModel(productId));
            if (null != productIndex) {
                productIndexRepository.save(productIndex);
                LOGGER.info(" 商品productId: {}, index ending ......", new Long[] { productId });
            }
        }
    }

    /**
     * 索引productIndex
     * @param productIds productIndex
     * @return ProductIndex
     */
    public void index(List<Long> productIds) throws ServiceException {
        String prodIds = StringUtils.join(productIds, ",");
        LOGGER.info(" 商品productIds: {}, index beginning ......", new String[] {prodIds});
        if (CollectionUtils.isNotEmpty(productIds)) {
            List<ProductImageModel> productImageModels = productService.listProductsImageModel(productIds);
            List<ProductIndex> productIndexes = Lists.newArrayList();
            for (ProductImageModel productImageModel : productImageModels) {
                ProductIndex productIndex = buildProductIndex(productImageModel);
                if (null != productIndex) {
                    productIndexes.add(productIndex);
                }
            }
            if (CollectionUtils.isNotEmpty(productIndexes)) {
                productIndexRepository.save(productIndexes);
            }
            LOGGER.info(" 商品productId: {}, index ending ......", new String[] { prodIds });
        }
    }

    /**
     * 索引productIndex
     * @param sns sns
     * @return ProductIndex
     */
    public void indexSns(List<String> sns) throws ServiceException {
        String snsStr = StringUtils.join(sns, ",");
        LOGGER.info(" sns: {}, index beginning ......", new String[] {snsStr});
        if (CollectionUtils.isNotEmpty(sns)) {
            List<ProductImageModel> productImageModels = productService.listBySns(sns);
            List<ProductIndex> productIndexes = Lists.newArrayList();
            for (ProductImageModel productImageModel : productImageModels) {
                ProductIndex productIndex = buildProductIndex(productImageModel);
                if (null != productIndex) {
                    productIndexes.add(productIndex);
                }
            }
            if (CollectionUtils.isNotEmpty(productIndexes)) {
                productIndexRepository.save(productIndexes);
            }
            LOGGER.info(" 商品sns: {}, index ending ......", new String[] { snsStr });
        }
    }

    private ProductIndex buildProductIndex(ProductImageModel productImageModel) {
        Long categoryId = productImageModel.getCategoryId();
        Long productId = productImageModel.getId();
        ProductIndex productIndex = null;
        if (null != categoryId) {
            categoryRepository.delete(productId);
            productIndex = new ProductIndex();
            productIndex.setAttributeOptionValueModels(Lists.newArrayList());
            BeanUtils.copyProperties(productImageModel, productIndex);
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
                    productIndex.getAttributeOptionValueModels().add(attributeOptionValueModel);
                }
            }

            if (StringUtils.isNotBlank(productIndex.getName())) {
                try {
                    productIndex.setNamePinYin(formatToPinYin(productIndex.getName()).toUpperCase());         //商品名称转拼音
                    productIndex.setNamePinYinAddr(formatAbbrToPinYin(productIndex.getName()).toUpperCase()); //商品名称转拼音首字母
                } catch (Exception ignored) {}
            }
            productIndex.setSearchStr(productIndex.getGoodsId() + "/" + productIndex.getErpGoodsId()
                                                         + "/" + productIndex.getSn() + "/" + productIndex.getName());

            productIndex.setTags(tags);
        }
        else {
            LOGGER.info(" 商品productId: {}, 分类categoryId为空，索引失败！", new Long[]{productId });
        }
        return productIndex;
    }

    /**
     * 删除全部索引
     *
     * @throws ServiceException
     */
    public void deleteIndex() throws ServiceException {
        LogWriter.append("delete", "start");

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
        LogWriter.append("delete", "end");
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
        Map<String, Object> attachData = new HashMap<>();

        if (StringUtils.isBlank(keyword) && null == categoryId) {
            return new PageModel(Lists.newArrayList(), 0, pageable);
        }

        NativeSearchQueryBuilder nativeSearchQueryBuilder;
        BoolQueryBuilder boolQueryBuilder = boolQuery();        //查询条件
        BoolFilterBuilder boolFilterBuilder = boolFilter();     //过滤条件

        if (StringUtils.isNotBlank(keyword) && keyword.matches("[A-Za-z0-9]+") && !specificWords.containsKey(keyword.toUpperCase())) {

            final String searchKeyword = "*" + keyword.toUpperCase() + "*";
            boolQueryBuilder.should(wildcardQuery("namePinYin", searchKeyword))
                            .should(wildcardQuery("namePinYinAddr", searchKeyword))
                            .minimumNumberShouldMatch(1);

        } else if (StringUtils.isNotBlank(keyword)) {

            final String searchKeyword;                 //搜索的关键词
            if (keyword.matches("[A-Za-z0-9]+") && specificWords.containsKey(keyword.toUpperCase())) {  //对特殊的搜索词进行转换
                searchKeyword = specificWords.get(keyword.toUpperCase());
            } else if (specificWords.containsKey(keyword)) {
                searchKeyword = specificWords.get(keyword);
            } else {
                searchKeyword = keyword;
            }

            if (isMatchMostName(searchKeyword)) {       //根据商品名称分词检索
                boolQueryBuilder.must(matchQuery("name", searchKeyword).analyzer("ik").minimumShouldMatch("85%"));
            }else{
                if (similarNames.containsKey(searchKeyword)) {
                    boolQueryBuilder.must(boolQuery().should(matchQuery("name", searchKeyword).analyzer("ik"))
                            .should(matchQuery("name", similarNames.get(searchKeyword)).minimumShouldMatch("100%"))
                            .minimumNumberShouldMatch(1));
                } else if (shouldMatchNames.contains(searchKeyword)) {
                    boolQueryBuilder.should(matchQuery("name", searchKeyword).analyzer("ik"));
                } else {
                    boolQueryBuilder.must(matchQuery("name", searchKeyword).analyzer("ik"));
                }
            }

            //判断搜索词是否包含品牌
            if (isBandName(searchKeyword)) {
                if (isspecificBandName(searchKeyword)) {     //判断品牌是否是其他品牌的子产品（如小浣熊）
                    boolQueryBuilder.should(matchQuery("brandName", searchKeyword).analyzer("ik"));
                } else {
                    boolQueryBuilder.must(matchQuery("brandName", searchKeyword).analyzer("ik"));       //查询品牌
                }
            }

            if (specificCategories.containsKey(searchKeyword)) {      //特殊搜索词指定分类指定搜索分类
                BoolQueryBuilder categoryQueryBuilder = boolQuery();
                specificCategories.get(searchKeyword).forEach(specificCategoryId ->
                        categoryQueryBuilder.should(termQuery("productCategoryIds", specificCategoryId)));
                boolQueryBuilder.must(categoryQueryBuilder.minimumNumberShouldMatch(1)).boost(2.0f);
            } else if (isCategoryName(searchKeyword)) {      //判断搜索词是否是分类
                BoolQueryBuilder categoryQueryBuilder = boolQuery();
                categoryRepository.findByNameLike(searchKeyword.replaceAll(" ", "")).forEach(category ->
                        categoryQueryBuilder.should(termQuery("productCategoryIds", category.getId())));
                boolQueryBuilder.must(categoryQueryBuilder.minimumNumberShouldMatch(1)).boost(2.0f);
            }

            //TODO 判断搜索词是否是标签
            boolQueryBuilder.should(nestedQuery("tags", matchQuery("tags.name", searchKeyword).analyzer("ik")));    //根据标签分词检索
            if (similarTags.containsKey(searchKeyword)) {          //将搜索词的同义词进行标签检索
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
        nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);

        boolFilterBuilder.must(termFilter("status", 0));

        if (null != categoryId) {       //限定商品分类
            boolFilterBuilder.must(termFilter("productCategoryIds", categoryId));
        }

        //排除结果中的指定分类
        if (StringUtils.isNotBlank(keyword) && exceptCategories.containsKey(keyword)) {
            exceptCategories.get(keyword).forEach(exceptCategoryId ->
                    boolFilterBuilder.mustNot(termFilter("productCategoryIds", exceptCategoryId)));
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
        }

        //获取第一个结果的分类号，并提高该分类商品的排名
//        elasticsearchTemplate.query(nativeSearchQueryBuilder.withPageable(new PageRequest(0, 1)).withMinScore(1f).withIndices("sjes").withTypes("products").build(), searchResponse -> {
//            if (searchResponse.getHits().getTotalHits() > 0) {
//                SearchHit[] searchHits = searchResponse.getHits().getHits();
//                nativeSearchQueryBuilder.withSort(SortBuilders.scriptSort("_score + (doc['categoryId'].value == myVal ? 1 : 0) * 2", "number").param("myVal", searchHits[0].getSource().get("categoryId")).order(SortOrder.DESC));  //使用Groovy脚本自定义排序
//            }
//            return null;
//        });

        if (null == categoryId) {
            if (StringUtils.isNotBlank(keyword)  && keyword.matches("[A-Za-z0-9]+")) {
                nativeSearchQueryBuilder.withMinScore(0.1f);
            }else {
                nativeSearchQueryBuilder.withMinScore(0.2f);
            }
        }

        final long[] totalHits = {0};   //总记录数
        FacetedPage<ProductIndex> queryForPage = elasticsearchTemplate.queryForPage(
                nativeSearchQueryBuilder.withPageable(new PageRequest(pageable.getPage(), pageable.getSize())).withIndices("sjes").withTypes("products")
                        .addAggregation(terms("categoryIdSet").field("categoryId"))
                        .withHighlightFields(new HighlightBuilder.Field("name").highlightQuery(matchQuery("name", keyword)).preTags("<b class=\"highlight\">").postTags("</b>")).build(), ProductIndex.class, new SearchResultMapper() {

                    @Override
                    public <T> FacetedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, org.springframework.data.domain.Pageable pageable) {
                        List<ProductIndex> productIndexes = new ArrayList<>();

                        totalHits[0] = searchResponse.getHits().getTotalHits();

                        //聚合结果中所有的分类Id
                        Set<Long> categoryIdSet = Sets.newHashSet();
                        Terms categoryIdAggr  = searchResponse.getAggregations().get("categoryIdSet");
                        categoryIdAggr.getBuckets().forEach(bucket -> categoryIdSet.add(bucket.getKeyAsNumber().longValue()));

                        if (searchResponse.getHits().getTotalHits() > 0) {
                            //final int[] i = {1};
                            searchResponse.getHits().forEach(searchHit -> {

                                //LOGGER.error((i[0]++) +"."+ searchHit.getSource().get("name") + "|" + searchHit.getSource().get("categoryId") + "|" + searchHit.getScore());

                                ProductIndex productIndex = null;
                                try {
                                    productIndex = (ProductIndex) mapToObject(aClass, searchHit.getSource());
                                } catch (IllegalAccessException | InstantiationException | IntrospectionException | InvocationTargetException e) {
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
                            attachData.put("categoryIdSet", categoryIdSet);
                        }

                        return new FacetedPageImpl<>((List<T>) productIndexes);
                    }
                });

        return new PageModel(queryForPage.getContent(), totalHits[0], attachData, pageable);

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

    /**
     * 判断关键词是否为品牌
     */
    private boolean isBandName(String keyword) {
        return elasticsearchTemplate.query(
                new NativeSearchQueryBuilder().withQuery(matchQuery("brandName", keyword).analyzer("ik")).withMinScore(0.01f)
                        .withPageable(new PageRequest(0, 1)).withIndices("sjes").withTypes("products").build(),
                searchBrandNameResponse -> searchBrandNameResponse.getHits().getTotalHits() > 0);
    }

    /**
     * 判断是否是特殊品牌名
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
     * 判断关键词是否是分类
     */
    private boolean isCategoryName(String keyword) {
        return categoryRepository.countByNameLike(keyword.replaceAll(" ", "")) > 0;
    }

    /**
     * 判断是否匹配大部分商品名
     */
    private boolean isMatchMostName(String keyword) {
        return keyword.length() > 3 && elasticsearchTemplate.query(
                new NativeSearchQueryBuilder().withQuery(matchQuery("name", keyword).analyzer("ik").minimumShouldMatch("85%"))
                        .withPageable(new PageRequest(0, 1)).withIndices("sjes").withTypes("products").build(),
                searchNameResponse -> searchNameResponse.getHits().getTotalHits() > 0);
    }
}
