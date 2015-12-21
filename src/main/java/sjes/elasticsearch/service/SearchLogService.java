package sjes.elasticsearch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.SearchLogModel;
import sjes.elasticsearch.repository.SearchLogRepository;

import java.time.LocalDateTime;

/**
 * Created by 白 on 2015/12/21.
 */
@Service("searchLogService")
public class SearchLogService {

    private static Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private SearchLogRepository searchLogRepository;

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
}
