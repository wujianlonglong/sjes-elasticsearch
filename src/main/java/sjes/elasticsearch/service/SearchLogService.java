package sjes.elasticsearch.service;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.HotWordModel;
import sjes.elasticsearch.domain.SearchLogModel;
import sjes.elasticsearch.repository.SearchLogRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;

/**
 * Created by 白 on 2015/12/21.
 */
@Service("searchLogService")
public class SearchLogService {

    private final static int MAX_HOTWORDS_COUNT = 5;    //能够获取的热门搜索记录的最大数量

    private static Logger LOGGER = LoggerFactory.getLogger(SearchLogService.class);

    @Autowired
    private SearchLogRepository searchLogRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 索引新的搜索记录
     *
     * @param keyword 搜索的关键字
     * @param categoryId 分类id
     * @param shopId 商店id
     * @param sortType 排序类型
     * @param startPrice 起始价格
     * @param endPrice 最高价格
     * @param userAgent 用户代理
     * @param ip
     * @throws ServiceException
     */
    public void index(String keyword, Long categoryId, String shopId, String sortType, Double startPrice, Double endPrice, String userAgent, String ip) throws ServiceException {
        if (StringUtils.isNotBlank(keyword)) {
            SearchLogModel searchLogModel = new SearchLogModel();
            searchLogModel.setKeyword(keyword);
            searchLogModel.setCategoryId(categoryId);
            searchLogModel.setShopId(shopId);
            searchLogModel.setSortType(sortType);
            searchLogModel.setStartPrice(startPrice);
            searchLogModel.setEndPrice(endPrice);
            searchLogModel.setUserAgent(userAgent);
            searchLogModel.setIp(ip);
            searchLogModel.setCreateDate(LocalDateTime.now());
            searchLogRepository.save(searchLogModel);
        }
    }

    /**
     * 删除所有搜索记录
     */
    public void deleteAll() {
        searchLogRepository.deleteAll();
    }

    /**
     * 获取热门搜索词
     *
     * @param maxCount 获取的热门搜索词的数量
     * @return 热门搜索词
     * @throws ServiceException
     */
    public List<HotWordModel> getHotWords(Integer maxCount) throws ServiceException {

        if (null == maxCount) {
            maxCount = MAX_HOTWORDS_COUNT;
        }

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .withSearchType(SearchType.COUNT)
                .withIndices("logstash-sjes").withTypes("searchlog")
                .addAggregation(terms("keywords").field("keyword"))
                .build();

        Aggregations aggregations = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse response) {
                return response.getAggregations();
            }
        });

        Terms keywords = aggregations.get("keywords");
        List<HotWordModel> hotWords = new ArrayList<>();
        int size = keywords.getBuckets().size();
        size = size > maxCount ? maxCount : size;                   //限制获取的热门搜索词数量
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                HotWordModel hotWordModel = new HotWordModel();
                hotWordModel.setKeyword(keywords.getBuckets().get(i).getKey());
                hotWordModel.setCount(keywords.getBuckets().get(i).getDocCount());
                hotWords.add(hotWordModel);
            }
        }
        return hotWords;
    }
}
