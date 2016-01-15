package sjes.elasticsearch.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import sjes.elasticsearch.domain.ProductIndex;

import java.util.List;

/**
 * Created by qinhailong on 15-12-8.
 */
public interface ProductIndexRepository extends ElasticsearchRepository<ProductIndex, Long> {

    List<ProductIndex> findByCategoryId(Long categoryId);
}
