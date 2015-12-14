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

//    @Autowired
//    private TagService tagService;

    @Autowired
    private ProductIndexService productIndexService;

    @Autowired
    private CategoryBasicAttributesService categoryBasicAttributesService;

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
//                List<Tag> tags = tagService.all();
//                Map<Long, Tag> tagMap = Maps.newHashMapWithExpectedSize(tags.size());
//                if (CollectionUtils.isNotEmpty(tags)) {
//                    tags.forEach(tag -> {
//                        tagMap.put(tag.getId(), tag);
//                    });
//                }

                Map<Long, ProductIndex> productMap = Maps.newHashMap();
                if (CollectionUtils.isNotEmpty(productIndexModels)) {
                    productIndexModels.forEach(productIndexModel -> {
                        ProductIndex productIndex = new ProductIndex();
                        productIndex.setTags(Lists.newArrayList());
                        productIndex.setAttributeOptionValueModels(Lists.newArrayList());
                        BeanUtils.copyProperties(productIndexModel, productIndex);

//                        List<ProductTag> productTags = productIndex.getProductTags();
//                        if (CollectionUtils.isNotEmpty(productTags)) {
//                            productTags.forEach(productTag -> {
//                                productIndex.getTags().add(tagMap.get(productTag.getTagId()));
//                            });
//                        }

                        Long parentId = null;
                        List<Tag> tags = productIndex.getTags();
                        int tagOrders = tags.size();
                        Tag tag = null;
                        Category category = categoryIdMap.get(productIndex.getCategoryId());
                        do {
                            parentId = category.getParentId();
                            if (Constants.CategoryGradeConstants.GRADE_ONE != category.getGrade() && null != parentId) {
                                category = categoryIdMap.get(parentId);
                                tag = new Tag();
                                tag.setName(category.getName());
                                tag.setOrders(tagOrders++);
                                tags.add(tag);
                            }
                            else if (null != parentId) {
                                parentId = null;
                            }
                        } while(null != parentId);
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
            List<CategoryIndex> categoryList = Lists.newArrayList(categoryIndexMap.values());
            // 分类查询条件
            categoryBasicAttributesService.saveOrUpdate(categoryList);
            return categoryList;
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
     * 查询分类商品列表
     *
     * @param keyword    关键字
     * @param categoryId 分类id
     * @param brandIds   品牌id
     * @param palceNames 地区
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
    public PageModel productSearch(String keyword, Long categoryId, String brandIds, String palceNames, String shopId, String sortType, String attributes, Boolean stock, Double startPrice, Double endPrice, Integer page, Integer size) throws ServiceException {
        // Pageable pageable = new Pageable(page, size);
        NativeSearchQueryBuilder nativeSearchQueryBuilder;
        boolean filterFlag = false; //判断是否需要过滤的标记

        //根据关键字查询商品
        if (StringUtils.isNotBlank(keyword)) {
            BoolQueryBuilder boolQueryBuilder = boolQuery().should(matchQuery("name", keyword).analyzer("ik"));    //根据商品名称检索，分析器为中文分词 ik，分数设置为5
            boolQueryBuilder.should(matchQuery("brandName", keyword));  //根据商品品牌名称搜索

            List<Category> categories = categorySearch(keyword);        //根据分类搜索
            if (categories != null) {
                categories.forEach(category -> boolQueryBuilder.should(termQuery("categoryId", category.getId())));
            }

            boolQueryBuilder.minimumNumberShouldMatch(1);
            nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);
        } else {
            nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(matchAllQuery());
        }

        BoolFilterBuilder boolFilterBuilder = boolFilter();

        if (null != categoryId) {       //限定商品分类
            boolFilterBuilder.must(termFilter("categoryId", categoryId));
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

        if (StringUtils.isNotBlank(palceNames)) {     //限定产地
            String[] palceNameArr = StringUtils.split(palceNames, "_");
            if (palceNameArr.length > 0) {
                BoolFilterBuilder palceNamesBoolFilterBuilder = boolFilter();
                for (String palceName : palceNameArr) {
                    palceNamesBoolFilterBuilder.should(termFilter("place", palceName));
                }
                boolFilterBuilder.must(palceNamesBoolFilterBuilder);
                filterFlag = true;
            }
        }

        if (null != startPrice) {    //限定最低价格
            boolFilterBuilder.must(rangeFilter("salePrice").gt(startPrice));
            filterFlag = true;
        }

        if (null != endPrice) {      //限定最高价格
            boolFilterBuilder.must(rangeFilter("salePrice").lt(endPrice));
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

        if (null != sortType) {       //排序
            SortBuilder sortBuilder = SortBuilders.fieldSort("salePrice").order(SortOrder.DESC);    //按价格逆序
            nativeSearchQueryBuilder.withSort(sortBuilder);
        }
        SearchQuery searchQuery = nativeSearchQueryBuilder.withPageable(new PageRequest(page, size)).build();
        Pageable pageable = new Pageable(page, size);
        FacetedPage<ProductIndex> facetedPage = productIndexRepository.search(searchQuery);
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

        SearchQuery searchQuery = nativeSearchQueryBuilder.withPageable(new PageRequest(page, size)).build();
        FacetedPage<Category> facetedPage = categoryRepository.search(searchQuery);
        return new PageModel(facetedPage.getContent(), facetedPage.getTotalElements(), new Pageable(page, size));
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
