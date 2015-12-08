package sjes.elasticsearch.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedFilterBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.CategoryIndex;
import sjes.elasticsearch.domain.PageModel;
import sjes.elasticsearch.domain.Pageable;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.repository.CategoryIndexModelRepository;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by qinhailong on 15-12-2.
 */
@Service("searchService")
public class SearchService {

    private static Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private CategoryIndexModelRepository categoryIndexModelRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryBasicAttributesService categoryBasicAttributesService;

    /**
     * 初始化索引
     */
    public  List<CategoryIndex> initService() throws ServiceException {
        LOGGER.debug("开始初始化索引！");
        try {
            List<CategoryIndex> categoryIndexes = categoryService.getCategoryIndexs();
            if (CollectionUtils.isNotEmpty(categoryIndexes)) {
                categoryIndexModelRepository.save(categoryIndexes);
                categoryBasicAttributesService.saveOrUpdate(categoryIndexes);
            }
            return categoryIndexes;
        } catch (Exception e) {
            LOGGER.error("初始化索引出现错误！", e);
            throw new ServiceException("初始化索引出现错误！", e.getCause());
        }
    }

    /**
     * 建立单个索引
     * @throws ServiceException
     */
    public void index(CategoryIndex categoryIndex) throws ServiceException {
        categoryIndexModelRepository.save(categoryIndex);
    }

    /**
     * 删除全部索引
     * @throws ServiceException
     */
    public void deleteIndex() throws ServiceException {
        categoryIndexModelRepository.deleteAll();
    }

    /**
     * 删除指定索引
     * @throws ServiceException
     */
    public void deleteIndex(Long categoryId) throws ServiceException {
        categoryIndexModelRepository.delete(categoryId);
    }

    /**
     * 删除指定索引
     * @throws ServiceException
     */
    public void deleteIndex(CategoryIndex categoryIndex) throws ServiceException {

        categoryIndexModelRepository.delete(categoryIndex);

    }

    /**
     * 查询分类商品列表
     * @param keyword 关键字
     * @param categoryId 分类id
     * @param brandId 品牌id
     * @param brandName 品牌名称
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
    public PageModel<ProductIndex> search(String keyword, Long categoryId, Long brandId, String brandName, String shopId, String sortType, String attributes, Boolean stock, Double startPrice, Double endPrice, Integer page, Integer size) throws ServiceException {
        Pageable pageable = new Pageable(page, size);
        List<ProductIndex> list = new ArrayList<>();
        SearchQuery searchQuery = null;

        if (StringUtils.isNotBlank(keyword)) { // 关键字查询

            BoolQueryBuilder boolQueryBuilder = boolQuery().should(matchQuery("name", keyword).analyzer("ik")).boost(5);    //根据分类名称检索，分析器为中文分词 ik，分数设置为5
            boolQueryBuilder.should(nestedQuery("productIndexes", matchQuery("productIndexes.name", keyword).analyzer("ik"))).boost(1);
            boolQueryBuilder.should(nestedQuery("productIndexes", matchQuery("productIndexes.brandName", keyword).analyzer("ik"))).boost(3);


            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);

            BoolFilterBuilder boolFilterBuilder = boolFilter();

            //添加过滤器
            if (null != categoryId) {
                boolFilterBuilder.must(termFilter("id", categoryId));
            }

            if(null != startPrice) {
                boolFilterBuilder.must(new NestedFilterBuilder("productIndexes", rangeFilter("productIndexes.salePrice").gte(startPrice)));
            }

            if(null != endPrice) {
                boolFilterBuilder.must(new NestedFilterBuilder("productIndexes", rangeFilter("productIndexes.salePrice").lte(endPrice)));
            }


            if(null != categoryId || null != startPrice || null != endPrice) {
                nativeSearchQueryBuilder.withFilter(boolFilterBuilder);
            }


            SortBuilder sortBuilder = SortBuilders.fieldSort("id").order(SortOrder.DESC);//排序

            //构造查询
            searchQuery = nativeSearchQueryBuilder.withSort(sortBuilder).build();
        } else if (null != categoryId) { // 分类商品列表

            BoolFilterBuilder boolFilterBuilder = boolFilter().must(termFilter("id", categoryId));

            if(null != brandId){
                boolFilterBuilder.must(new NestedFilterBuilder("productIndexes", termFilter("productIndexes.brandId", brandId)));
            }

            searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).withFilter(boolFilterBuilder).build();
        }

        //return new PageModel<> (Lists.newArrayList(), 0, pageable);

        List<CategoryIndex> categoryIndexList = categoryIndexModelRepository.search(searchQuery).getContent();
        LOGGER.info(categoryIndexList.size() + "");
        if (categoryIndexList.size() > 0) {
            for (CategoryIndex categoryIndex : categoryIndexList) {
                list.addAll(categoryIndex.getProductIndexes());
            }
        }

        return new PageModel<>(list, 0, pageable);
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
