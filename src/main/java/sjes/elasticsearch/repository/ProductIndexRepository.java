package sjes.elasticsearch.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import sjes.elasticsearch.domain.ProductIndex;

/**
 * Created by qinhailong on 15-12-8.
 */
public interface ProductIndexRepository extends ElasticsearchRepository<ProductIndex, Long> {
}
