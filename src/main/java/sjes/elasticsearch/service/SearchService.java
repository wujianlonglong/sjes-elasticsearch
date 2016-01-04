package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;

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
    /**
     * 初始化索引
     */
    public List<CategoryIndex> initService() throws ServiceException {
        LOGGER.debug("开始初始化索引！");
        try {
            List<Category> thirdCategories = Lists.newArrayList();
            Map<Long, CategoryIndex> categoryIndexMap = Maps.newHashMap();
            List<Category> allCategories = categoryService.all();
            Map<Long, Category> categoryIdMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(allCategories)) {
                allCategories.forEach(category -> {
                    Integer grade = category.getGrade();
                    if (null != grade) {
                        if (Constants.CategoryGradeConstants.GRADE_THREE == grade.intValue()) {
                            thirdCategories.add(category);
                        }
                    }
                    categoryIdMap.put(category.getId(), category);
                });
            }
            if (CollectionUtils.isNotEmpty(thirdCategories)) {
                // 分类索引
                categoryRepository.save(thirdCategories);
                thirdCategories.forEach(thirdCategory -> {
                    CategoryIndex categoryIndex = new CategoryIndex();
                    categoryIndex.setProductIndexes(Lists.newArrayList());
                    BeanUtils.copyProperties(thirdCategory, categoryIndex);
                    categoryIndexMap.put(thirdCategory.getId(), categoryIndex);
                });
                List<Long> categoryIds = Lists.newArrayList(categoryIndexMap.keySet());
                List<ProductImageModel> productImageModels = productService.listByCategoryIds(categoryIds);
                List<AttributeModel> attributeModels = attributeService.lists(categoryIds);
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
                List<Brand> brands = brandService.listAll();
                Map<Long, String> brandNameMap = Maps.newHashMap();
                if (CollectionUtils.isNotEmpty(brands)) {
                    brands.forEach(brand->{
                        brandNameMap.put(brand.getBrandId(), brand.getName());
                    });
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
                        productIndex.getProductCategoryIds().add(categoryId);
                        this.populateCategoryTag(categoryIdMap, productIndex, categoryId);
                    });
                }
                List<ProductAttributeValue> productAttributeValues = productAttributeValueService.listByProductIds(Lists.newArrayList(productMap.keySet()));
                if (CollectionUtils.isNotEmpty(productAttributeValues)) {
                    productAttributeValues.forEach(productAttributeValue -> {
                        ProductIndex productIndex = productMap.get(productAttributeValue.getProductId());
                        List<Tag> tags = productIndex.getTags();
                        Tag tag = new Tag();
                        tag.setName(productAttributeValue.getAttributeName());
                        tag.setOrders(tags.size());
                        tags.add(tag);
                        AttributeOptionValueModel attributeOptionValueModel = new AttributeOptionValueModel();
                        Attribute attribute = attributeMaps.get(productAttributeValue.getAttributeId());
                        AttributeOption attributeOption = attributeOptionMaps.get(productAttributeValue.getAttributeOptionId());
                        if (attribute != null) {
                            BeanUtils.copyProperties(attribute, attributeOptionValueModel);
                        }
                        attributeOptionValueModel.setAttributeOption(attributeOption);
                        productIndex.getAttributeOptionValueModels().add(attributeOptionValueModel);
                    });
                }
                // productIndex索引
                List<ProductIndex> productIndexes = Lists.newArrayList(productMap.values());
                productIndexService.saveBat(productIndexes);
            }
            return Lists.newArrayList(categoryIndexMap.values());
        } catch (Exception e) {
            LOGGER.error("初始化索引出现错误！", e);
            throw new ServiceException("初始化索引出现错误！", e.getCause());
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
        do {
            Category category = categoryIdMap.get(categoryId);
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
        if (null != productId) {
            categoryRepository.delete(productId);
            ProductImageModel productImageModel = productService.getProductImageModel(productId);
            ProductIndex productIndex = new ProductIndex();
            productIndex.setAttributeOptionValueModels(Lists.newArrayList());
            BeanUtils.copyProperties(productImageModel, productIndex);
            Long categoryId = productIndex.getCategoryId();
            List<Category> categories = categoryService.findClusters(categoryId);
            List<Tag> tags = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(categories)) {
                categories.forEach(category -> {
                    Tag tag = new Tag();
                    tag.setName(category.getName());
                    tag.setOrders(category.getGrade() - 1);
                    tags.add(tag);
                });
            }
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
                    Tag tag = new Tag();
                    tag.setName(productAttributeValue.getAttributeName());
                    tag.setOrders(tags.size());
                    tags.add(tag);
                    AttributeOptionValueModel attributeOptionValueModel = new AttributeOptionValueModel();
                    Attribute attribute = attributeMaps.get(productAttributeValue.getAttributeId());
                    AttributeOption attributeOption = attributeOptionMaps.get(productAttributeValue.getAttributeOptionId());
                    BeanUtils.copyProperties(attribute, attributeOptionValueModel);
                    attributeOptionValueModel.setAttributeOption(attributeOption);
                    productIndex.getAttributeOptionValueModels().add(attributeOptionValueModel);
                });
            }
            productIndex.setTags(tags);
            productIndexRepository.save(productIndex);
        }
    }

    /**
     * 删除全部索引
     *
     * @throws ServiceException
     */
    public void deleteIndex() throws ServiceException {
        categoryRepository.deleteAll();
        productIndexRepository.deleteAll();
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
     * 查询分类商品列表
     *
     * @param keyword    关键字
     * @param categoryId 分类id
     * @param brandIds   品牌id
     * @param placeNames 地区
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
    public PageModel productSearch(String keyword, Long categoryId, String brandIds, String placeNames, String shopId, String sortType, String attributes, Boolean stock, Double startPrice, Double endPrice, Integer page, Integer size) throws ServiceException {
        Pageable pageable = new Pageable(page, size);

        if (StringUtils.isBlank(keyword) && null == categoryId) {
            return new PageModel(Lists.newArrayList(), 0, pageable);
        }

        NativeSearchQueryBuilder nativeSearchQueryBuilder;
        BoolQueryBuilder boolQueryBuilder = boolQuery();
        boolean filterFlag = false; //判断是否需要过滤的标记

        //根据关键字查询商品
        if (StringUtils.isNotBlank(keyword)) {
            boolQueryBuilder.must(matchQuery("name", keyword).analyzer("ik"));                //根据商品名称分词检索
            boolQueryBuilder.should(matchQuery("name", keyword).analyzer("ik").minimumShouldMatch("80%"));                //根据商品名称分词检索
            boolQueryBuilder.should(nestedQuery("tags", matchQuery("tags.name", keyword).analyzer("ik")));  //根据标签分词检索

            //先判断输入的关键字是否为品牌，是则作为必须条件
            elasticsearchTemplate.query(
                    new NativeSearchQueryBuilder().withQuery(matchQuery("brandName", keyword).analyzer("ik")).withMinScore(0.01f).withPageable(new PageRequest(0, 1)).withIndices("sjes").withTypes("products").build(),
                    searchBrandNameResponse -> {
                        //LOGGER.info(searchBrandNameResponse.getHits().getMaxScore()+"");
                        if (searchBrandNameResponse.getHits().getTotalHits() > 0){
                            boolQueryBuilder.must(matchQuery("brandName", keyword).analyzer("ik"));           //根据商品品牌名称搜索
                        }
                        return null;
                    });

            //匹配分类标签名来获取最有可能的分类
            elasticsearchTemplate.query(new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("tagName", keyword).minimumShouldMatch("50%").analyzer("ik")).withPageable(new PageRequest(0, 2)).withMinScore(1f).withIndices("sjes").withTypes("categories").build(), searchResponse -> {
                if (searchResponse.getHits().getTotalHits() > 0) {
                    SearchHit[] searchHits = searchResponse.getHits().getHits();
                    for (int i = 0; i < searchHits.length; i++) {
                        boolQueryBuilder.should(termQuery("categoryId", searchHits[i].getSource().get("id")));  //根据分类查询
                    }
                }
                return null;
            });
        } else {
            boolQueryBuilder.should(matchAllQuery());
        }

        if (StringUtils.isNotBlank(placeNames)) {     //限定产地
            String[] placeNameArr = StringUtils.split(placeNames, "_");
            if (placeNameArr.length > 0) {
                BoolQueryBuilder palceNamesBoolQueryBuilder = boolQuery();
                for (String placeName : placeNameArr) {
                    palceNamesBoolQueryBuilder.should(wildcardQuery("place", "*"+placeName+"*"));
                }
                boolQueryBuilder.must(palceNamesBoolQueryBuilder);
            }
        }

        nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);

        //调试用，各项结果分数
//        elasticsearchTemplate.query(nativeSearchQueryBuilder.withPageable(new PageRequest(0, 500)).build(), searchResponse -> {
//            SearchHit[] searchHits = searchResponse.getHits().getHits();
//            for(int i=0;i<searchHits.length;i++){
//                LOGGER.info((i+1) + "、 "+searchHits[i].getSource().get("name") + "  →  "+searchHits[i].getScore());
//            }
//            return null;
//        });

        BoolFilterBuilder boolFilterBuilder = boolFilter();

        if (null != categoryId) {       //限定商品分类
            BoolFilterBuilder categoryIdBoolFilterBuilder = boolFilter().should(termFilter("categoryId", categoryId));
            List<Category> categories = categorySearch(categoryId);
            if (null != categories) {
                categories.forEach(category -> categoryIdBoolFilterBuilder.should(termFilter("categoryId", category.getId())));
            }
            boolFilterBuilder.must(categoryIdBoolFilterBuilder);
            filterFlag = true;
        }

        if (StringUtils.isNotBlank(brandIds)) {     //限定品牌
            String[] brandIdArr = StringUtils.split(brandIds, "_");
            if (brandIdArr.length > 0) {
                BoolFilterBuilder brandIdsBoolFilterBuilder = boolFilter();
                for (String brandId : brandIdArr) {
                    brandIdsBoolFilterBuilder.should(termFilter("brandId", brandId));
                }
                boolFilterBuilder.must(brandIdsBoolFilterBuilder);
                filterFlag = true;
            }
        }

        if (null != startPrice) {    //限定最低价格
            boolFilterBuilder.must(rangeFilter("memberPrice").gt(startPrice));
            filterFlag = true;
        }

        if (null != endPrice) {      //限定最高价格
            boolFilterBuilder.must(rangeFilter("memberPrice").lt(endPrice));
            filterFlag = true;
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

                        filterFlag = true;
                    }
                }
            }
        }

        if (filterFlag) {   //判断是否有限定条件
            nativeSearchQueryBuilder.withFilter(boolFilterBuilder);
        }

        if (null != sortType && !sortType.equals("default")) {       //排序
            SortBuilder sortBuilder = null;
            if(sortType.equals("sales")) {  //销量降序
                sortBuilder = SortBuilders.fieldSort("sales").order(SortOrder.DESC);
            }else if(sortType.equals("salesUp")) {  //销量升序
                sortBuilder = SortBuilders.fieldSort("sales").order(SortOrder.ASC);
            }else if(sortType.equals("price")) {  //价格降序
                sortBuilder = SortBuilders.fieldSort("memberPrice").order(SortOrder.DESC);
            }else if(sortType.equals("priceUp")) {  //销价格升序
                sortBuilder = SortBuilders.fieldSort("memberPrice").order(SortOrder.ASC);
            }
            nativeSearchQueryBuilder.withSort(sortBuilder);
        } else {
            //获取第一个结果的分类号，并提高该分类商品的排名
            elasticsearchTemplate.query(nativeSearchQueryBuilder.withPageable(new PageRequest(0, 1)).withMinScore(1f).withIndices("sjes").withTypes("products").build(), searchResponse -> {
                if (searchResponse.getHits().getTotalHits() > 0) {
                    SearchHit[] searchHits = searchResponse.getHits().getHits();
                    nativeSearchQueryBuilder.withSort(SortBuilders.scriptSort("_score + (doc['categoryId'].value == myVal ? 1 : 0) * 1", "number").param("myVal", searchHits[0].getSource().get("categoryId")).order(SortOrder.DESC));  //使用Groovy脚本自定义排序
                }
                return null;
            });
        }

        FacetedPage<ProductIndex> facetedPage = productIndexRepository.search(nativeSearchQueryBuilder.withPageable(new PageRequest(pageable.getPage(), pageable.getSize())).withMinScore(0.35f).build());
        return new PageModel(facetedPage.getContent(), facetedPage.getTotalElements(), pageable);

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
     * 搜索相关分类
     *
     * @param keyword 关键字
     * @return 分类列表
     * @throws ServiceException
     */
    public List<Category> categorySearch(String keyword) throws ServiceException {
        NativeSearchQueryBuilder nativeSearchQueryBuilder;

        if (StringUtils.isEmpty(keyword)) {
            return null;
        }

        nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(wildcardQuery("name", "*"+keyword+"*"));
        SearchQuery searchQuery = nativeSearchQueryBuilder.build();
        List<Category> categories = categoryRepository.search(searchQuery).getContent();

        return categories;
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
     * 获取最有可能的分类
     * @param boolQueryBuilder 查询条件
     * @return 分类id
     * @throws ServiceException
     */
    public Long getPossibleCategoryId(BoolQueryBuilder boolQueryBuilder) throws ServiceException {
        if(null == boolQueryBuilder){
            return -1L;
        }

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                        .withMinScore(0.5f)
                        .withIndices("sjes").withTypes("products")
                        .addAggregation(terms("categoryIds").field("categoryId"))
                        .build();

        Aggregations aggregations = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse response) {
                return response.getAggregations();
            }
        });

        Terms categoryIds = aggregations.get("categoryIds");
        if(categoryIds.getBuckets().size() > 0) {
            return Long.valueOf(categoryIds.getBuckets().get(0).getKey());
        }else{
            return -1L;
        }
    }

    /**
     * 获取最有可能的标签（以商品名称搜索获取相关标签最多）
     * @param keyword 关键字
     * @param matchCount 定义匹配的标签数
     * @return 标签
     * @throws ServiceException
     */
    public List<String> getPossibleTags(String keyword, int matchCount) throws ServiceException {

        List<String> list = new ArrayList<>();

        if(StringUtils.isBlank(keyword) || matchCount < 1){
            return list;
        }

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("name", keyword))
                .withSearchType(SearchType.COUNT)
                .withIndices("sjes").withTypes("products")
                .addAggregation(AggregationBuilders.nested("tags").path("tags").subAggregation(AggregationBuilders.terms("tags").field("tags.name")))
                .build();

        Aggregations aggregations = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse response) {
                return response.getAggregations();
            }
        });

        Nested nested = aggregations.get("tags");
        Terms terms = nested.getAggregations().get("tags");
        int size = terms.getBuckets().size();
        size = size > matchCount ? matchCount : 0;                             //控制标签对于搜索的影响
        for (int i = 0; i < size; i++) {
            list.add(terms.getBuckets().get(i).getKey());
        }
        return list;
    }

}
