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
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.*;
import sjes.elasticsearch.feigns.category.model.*;
import sjes.elasticsearch.feigns.item.model.ProductAttributeValue;
import sjes.elasticsearch.feigns.item.model.ProductTag;
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
    private ProductIndexRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductAttributeValueService productAttributeValueService;

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ProductIndexService productIndexService;

    @Autowired
    private CategoryBasicAttributesService categoryBasicAttributesService;

    /**
     * 初始化索引
     */
    public  List<CategoryIndex> initService() throws ServiceException {
        LOGGER.debug("开始初始化索引！");
        try {
            List<Category> categories = categoryService.listByGradeThree();
            Map<Long, CategoryIndex> categoryIndexMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(categories)) {
                // 分类索引
                categoryRepository.save(categories);
                categories.forEach(category -> {
                    CategoryIndex categoryIndex = new CategoryIndex();
                    categoryIndex.setProductIndexes(Lists.newArrayList());
                    BeanUtils.copyProperties(category, categoryIndex);
                    categoryIndexMap.put(category.getId(), categoryIndex);
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
                List<Tag> tags = tagService.all();
                Map<Long, Tag> tagMap = Maps.newHashMapWithExpectedSize(tags.size());
                if (CollectionUtils.isNotEmpty(tags)) {
                    tags.forEach(tag -> {
                        tagMap.put(tag.getId(), tag);
                    });
                }
                Map<Long, ProductIndex> productMap = Maps.newHashMap();
                if (CollectionUtils.isNotEmpty(productIndexModels)) {
                    productIndexModels.forEach(productIndexModel -> {
                        ProductIndex productIndex = new ProductIndex();
                        productIndex.setTags(Lists.newArrayList());
                        productIndex.setAttributeOptionValueModels(Lists.newArrayList());
                        BeanUtils.copyProperties(productIndexModel, productIndex);
                        List<ProductTag> productTags = productIndex.getProductTags();
                        if (CollectionUtils.isNotEmpty(productTags)) {
                            productTags.forEach(productTag -> {
                                productIndex.getTags().add(tagMap.get(productTag.getTagId()));
                            });
                        }
                        categoryIndexMap.get(productIndexModel.getCategoryId()).getProductIndexes().add(productIndex);
                        productMap.put(productIndexModel.getId(), productIndex);
                    });
                }
                List<ProductAttributeValue> productAttributeValues = productAttributeValueService.listByProductIds(Lists.newArrayList(productMap.keySet()));
                if (CollectionUtils.isNotEmpty(productAttributeValues)) {
                    productAttributeValues.forEach(productAttributeValue -> {
                        AttributeOptionValueModel attributeOptionValueModel = new AttributeOptionValueModel();
                        Attribute attribute = attributeNameMaps.get(productAttributeValue.getAttributeId());
                        AttributeOption attributeOption = attributeOptionValueMaps.get(productAttributeValue.getAttributeOptionId());
                        BeanUtils.copyProperties(attribute, attributeOptionValueModel);
                        attributeOptionValueModel.setAttributeOption(attributeOption);
                        productMap.get(productAttributeValue.getProductId()).getAttributeOptionValueModels().add(attributeOptionValueModel);
                    });
                }
                // productIndex索引
                productIndexService.saveBat(Lists.newArrayList(productMap.values()));
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
     * @throws ServiceException
     */
    public void deleteIndex() throws ServiceException {
        categoryRepository.deleteAll();
        productRepository.deleteAll();
    }

    /**
     * 查询分类商品列表
     * @param keyword 关键字
     * @param categoryId 分类id
     * @param brandIds 品牌id
     * @param palceNames 地区
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
    public PageModel<ProductIndex> productSearch(String keyword, Long categoryId, String brandIds, String palceNames, String shopId, String sortType, String attributes, Boolean stock, Double startPrice, Double endPrice, Integer page, Integer size) throws ServiceException {
        if (StringUtils.isNotBlank(attributes)) {
            String[] attrs = StringUtils.split(attributes, "_");
            if (attrs.length > 0) {
                for (String attr : attrs) {
                    String[] attrValues = StringUtils.split(attr, "-");
                    if (attrValues.length == 2) {
                        String attributeId = attrValues[0];
                        String attributeOptionId = attrValues[1];

                    }
                }
            }
        }
        Pageable pageable = new Pageable(page, size);
        NativeSearchQueryBuilder nativeSearchQueryBuilder;
        boolean filterFlag = false; //判断是否需要过滤的标记

        //根据关键字查询商品
        if (StringUtils.isNotBlank(keyword)) {
            BoolQueryBuilder boolQueryBuilder = boolQuery().should(matchQuery("name", keyword).analyzer("ik"));    //根据商品名称检索，分析器为中文分词 ik，分数设置为5
            boolQueryBuilder.should(matchQuery("brandName", keyword));  //根据商品品牌名称搜索
//            boolQueryBuilder.should(nestedQuery("productIndexes", matchQuery("productIndexes.name", keyword).analyzer("ik"))).boost(1);

            boolQueryBuilder.minimumNumberShouldMatch(1);
            nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);
        } else {
            nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(matchAllQuery());
        }

        BoolFilterBuilder boolFilterBuilder = boolFilter();

        if (null != categoryId) {   //限定商品分类
            boolFilterBuilder.must(termFilter("categoryId", categoryId));
            filterFlag = true;
        }

//        if (null != brandName) {      //限定品牌
//            boolFilterBuilder.must(termFilter("brandName", brandName));
//            filterFlag = true;
//        }

        if (StringUtils.isNotBlank(brandIds)) { //限定品牌
            String[] brandIdArr = StringUtils.split(attributes, "_");
            for (String brandId : brandIdArr) {
                boolFilterBuilder.must(termFilter("brandId", brandId));
                filterFlag = true;
            }
        }

        // TODO 限制地区
//        if (StringUtils.isNotBlank(palceNames)) {
//            String[] palceNameArr = StringUtils.split(palceNames, "_");
//            for (String palceName : palceNameArr) {
//                boolFilterBuilder.must(termFilter("palce", palceName));
//                filterFlag = true;
//            }
//        }
//        if (null != brandId) {        //限定品牌
//            boolFilterBuilder.must(termFilter("brandId", brandId));
//            filterFlag = true;
//        }

        if (null != startPrice) {    //限定最低价格
            boolFilterBuilder.must(rangeFilter("salePrice").gt(startPrice));
            filterFlag = true;
        }

        if (null != endPrice) {      //限定最高价格
            boolFilterBuilder.must(rangeFilter("salePrice").lt(endPrice));
            filterFlag = true;
        }

        if (filterFlag) {
            nativeSearchQueryBuilder.withFilter(boolFilterBuilder);
        }

        if (null != sortType) {       //排序
            SortBuilder sortBuilder = SortBuilders.fieldSort("salePrice").order(SortOrder.DESC);    //按价格逆序
            nativeSearchQueryBuilder.withSort(sortBuilder);
        }

        SearchQuery searchQuery = nativeSearchQueryBuilder.build();

        List<ProductIndex> productIndexes = productRepository.search(searchQuery).getContent();
        LOGGER.info("found products size:" + productIndexes.size());

        return new PageModel<>(productIndexes, 0, pageable);
    }

    /**
     * 查询分类
     * @param keyword 关键字
     * @param categoryId 分类 id
     * @param page 页面
     * @param size 页面大小
     * @return 分类信息
     * @throws ServiceException
     */
    public PageModel<Category> categorySearch(String keyword, Long categoryId, Integer page, Integer size) throws ServiceException {
        Pageable pageable = new Pageable(page, size);
        NativeSearchQueryBuilder nativeSearchQueryBuilder;
        boolean filterFlag = false; //判断是否需要过滤的标记

        if(StringUtils.isNotBlank(keyword)){
            BoolQueryBuilder boolQueryBuilder = boolQuery().should(matchQuery("name", keyword).analyzer("ik"));
            nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);
        }else{
            nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(matchAllQuery());
        }

        BoolFilterBuilder boolFilterBuilder = boolFilter();

        if (null != categoryId) {
            boolFilterBuilder.must(termFilter("id", categoryId));
            filterFlag = true;
        }

        if(filterFlag){
            nativeSearchQueryBuilder.withFilter(boolFilterBuilder);
        }

        SearchQuery searchQuery = nativeSearchQueryBuilder.build();

        List<Category> categories = categoryRepository.search(searchQuery).getContent();
        LOGGER.info("found categories size:" + categories.size());

        return new PageModel<>(categories, 0, pageable);
    }


    /**
     * 搜索建议，自动补全搜索结结果
     * @param indices 索引库名称
     * @param prefix 搜索前缀词
     * @return 建议列表
     */
//    public static List<String> getCompletionSuggest(String indices,
//                                                    String prefix) {
//        CompletionSuggestionBuilder suggestionsBuilder = new CompletionSuggestionBuilder(
//                "complete");
//        suggestionsBuilder.text(prefix);
//        suggestionsBuilder.field("suggest");
//        suggestionsBuilder.size(10);
//        SuggestResponse resp = client.prepareSuggest(indices)
//                .addSuggestion(suggestionsBuilder).execute().actionGet();
//        List<? extends Entry<? extends Option>> list = resp.getSuggest()
//                .getSuggestion("complete").getEntries();
//        List<String> suggests = new ArrayList<String>();
//        if (list == null) {
//            return null;
//        } else {
//            for (Entry<? extends Option> e : list) {
//                for (Option option : e) {
//                    suggests.add(option.getText().toString());
//                }
//            }
//            return suggests;
//        }
//    }
}
