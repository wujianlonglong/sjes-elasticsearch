package sjes.elasticsearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.feigns.category.feign.CategoryFeign;
import sjes.elasticsearch.feigns.category.model.Category;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Service("categoryService")
public class CategoryService {

    @Autowired
    private CategoryFeign categoryFeign;

    /**
     * 根据分类id查询分类簇对象
     *
     * @param categoryId 　分类id
     * @return　分类簇对象
     */
    public List<Category> findClusters(Long categoryId) {
        return categoryFeign.findClusters(categoryId);
    }

    /**
     * 查询所有分类列表
     * @return 分类列表
     */
    public List<Category> all() {
        return  categoryFeign.list(new Category());
    }

}
