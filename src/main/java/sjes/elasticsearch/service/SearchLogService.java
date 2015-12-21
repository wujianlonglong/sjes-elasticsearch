package sjes.elasticsearch.service;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    private static Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private SearchLogRepository searchLogRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 索引新的搜索记录
     * @param keyword 搜索关键字
     * @throws ServiceException
     */
    @RequestMapping(method = RequestMethod.PUT)
    public void index(String keyword) throws ServiceException {
        SearchLogModel searchLogModel = new SearchLogModel();
        searchLogModel.setKeyword(keyword);
        searchLogModel.setCreateDate(LocalDateTime.now());
        searchLogRepository.save(searchLogModel);
    }

    /**
     * 获取热门搜索词
     * @return 热门搜索词
     * @throws ServiceException
     */
    public List<HotWordModel> getHotWords(Integer maxCount) throws ServiceException {

        if(null == maxCount){
            maxCount = 5;
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
        if(size > 0) {
            for (int i=0;i<size;i++){
                HotWordModel hotWordModel = new HotWordModel();
                hotWordModel.setKeyword(keywords.getBuckets().get(i).getKey());
                hotWordModel.setCount(keywords.getBuckets().get(i).getDocCount());
                hotWords.add(hotWordModel);
            }
        }
        return hotWords;
    }
}
