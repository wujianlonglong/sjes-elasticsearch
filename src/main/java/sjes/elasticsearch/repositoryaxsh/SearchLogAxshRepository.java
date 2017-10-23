package sjes.elasticsearch.repositoryaxsh;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import sjes.elasticsearch.domainaxsh.SearchLogModelAxsh;

/**
 * Created by qinhailong on 15-12-3.
 */
public interface SearchLogAxshRepository extends ElasticsearchRepository<SearchLogModelAxsh, Long> {
}
