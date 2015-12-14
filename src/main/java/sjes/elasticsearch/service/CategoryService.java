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
     * 查询所有分类列表
     * @return 分类列表
     */
    public List<Category> all() {
        return  categoryFeign.list(new Category());
    }

}
