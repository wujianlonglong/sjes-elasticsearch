package sjes.elasticsearch.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import sjes.elasticsearch.domain.CategoryIndex;

/**
 * Created by qinhailong on 15-12-3.
 */
public interface CategoryIndexModelRepository extends ElasticsearchRepository<CategoryIndex, String> {
}
