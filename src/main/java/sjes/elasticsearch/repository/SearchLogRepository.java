package sjes.elasticsearch.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import sjes.elasticsearch.domain.SearchLogModel;

/**
 * Created by qinhailong on 15-12-3.
 */
public interface SearchLogRepository extends ElasticsearchRepository<SearchLogModel, Long> {
}
