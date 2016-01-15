package sjes.elasticsearch.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import sjes.elasticsearch.feigns.category.model.Category;

import java.util.List;

/**
 * Created by qinhailong on 15-12-3.
 */
public interface CategoryRepository extends ElasticsearchRepository<Category, Long> {
    List<Category> findByName(String name);
}
