package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.domain.*;
import sjes.elasticsearch.feigns.category.model.*;
import sjes.elasticsearch.feigns.item.model.ProductAttributeValue;
import sjes.elasticsearch.repository.CategoryRepository;
import sjes.elasticsearch.repository.ProductIndexRepository;

import java.util.List;
import java.util.Map;

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
                List<ProductIndexModel> productIndexModels = productService.listByCategoryIds(categoryIds);

                List<AttributeModel> attributeModels = attributeService.lists(categoryIds);
                Map<Long, Attribute> attributeNameMaps = Maps.newHashMap();
                Map<Long, AttributeOption> attributeOptionValueMaps = Maps.newHashMap();
                if (CollectionUtils.isNotEmpty(attributeModels)) {
                    attributeModels.forEach(attributeModel -> {
                        attributeNameMaps.put(attributeModel.getId(), attributeModel);
                        List<AttributeOption> attributeOptions = attributeModel.getAttributeOptions();
                        if (CollectionUtils.isNotEmpty(attributeOptions)) {
                            attributeOptions.forEach(attributeOption -> {
                                attributeOptionValueMaps.put(attributeOption.getId(), attributeOption);
                            });
                        }
                    });
                }

                Map<Long, ProductIndex> productMap = Maps.newHashMap();
                if (CollectionUtils.isNotEmpty(productIndexModels)) {
                    productIndexModels.forEach(productIndexModel -> {
                        ProductIndex productIndex = new ProductIndex();
                        productIndex.setTags(Lists.newArrayList());
                        productIndex.setAttributeOptionValueModels(Lists.newArrayList());
                        BeanUtils.copyProperties(productIndexModel, productIndex);

                        Long categoryId = productIndex.getCategoryId();
                        List<Tag> tags = productIndex.getTags();
                        int tagOrders = tags.size();
                        Tag tag = null;
                        do {
                            Category category = categoryIdMap.get(categoryId);
                            if (null != category) {
                                tag = new Tag();
                                tag.setName(category.getName());
                                tag.setOrders(tagOrders + category.getGrade() - 1);
                                tags.add(tag);
                                categoryId = category.getParentId();
                            }
                            else {
                                categoryId = null;
                            }
                        } while(null != categoryId);
                        categoryIndexMap.get(productIndexModel.getCategoryId()).getProductIndexes().add(productIndex);
                        productMap.put(productIndexModel.getId(), productIndex);
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
                        Attribute attribute = attributeNameMaps.get(productAttributeValue.getAttributeId());
                        AttributeOption attributeOption = attributeOptionValueMaps.get(productAttributeValue.getAttributeOptionId());
                        BeanUtils.copyProperties(attribute, attributeOptionValueModel);
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
     * 删除全部索引
     *
     * @throws ServiceException
     */
    public void deleteIndex() throws ServiceException {
        categoryRepository.deleteAll();
        productIndexRepository.deleteAll();
    }

    /**
     * 根据分类id得到ProductIndex
     * @param categoryId 分类id
     * @return ProductIndex
     */
    public ProductIndex getProductIndexByCategoryId(Long categoryId) {
        if (null != categoryId) {

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
            boolQueryBuilder.should(matchQuery("name", keyword).analyzer("ik"));    //根据商品名称检索，分析器为中文分词 ik
            boolQueryBuilder.should(termQuery("name", keyword).boost(2));    //根据商品名称检索
            boolQueryBuilder.should(matchQuery("brandName", keyword).boost(3));  //根据商品品牌名称搜索，分数设为3
            boolQueryBuilder.should(nestedQuery("tags", matchQuery("tags.name", keyword)));  //根据商品品牌名称搜索
            List<Category> categories = categorySearch(keyword);        //根据分类搜索
            if (categories != null) {
                categories.forEach(category -> boolQueryBuilder.should(termQuery("categoryId", category.getId())));
            }

            boolQueryBuilder.minimumNumberShouldMatch(1);
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
        }
        FacetedPage<ProductIndex> facetedPage = productIndexRepository.search(nativeSearchQueryBuilder.withPageable(new PageRequest(pageable.getPage(), pageable.getSize())).build());
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

        nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(matchAllQuery());

        BoolFilterBuilder boolFilterBuilder = boolFilter();
        boolFilterBuilder.must(termFilter("name", keyword));
        nativeSearchQueryBuilder.withFilter(boolFilterBuilder);
        SearchQuery searchQuery = nativeSearchQueryBuilder.build();
        List<Category> categories = categoryRepository.search(searchQuery).getContent();

        return categories;
    }
}
