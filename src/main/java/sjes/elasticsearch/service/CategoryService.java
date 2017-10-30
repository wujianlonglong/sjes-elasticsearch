package sjes.elasticsearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import sjes.elasticsearch.common.ResponseMessage;
import sjes.elasticsearch.feigns.category.feign.CategoryFeign;
import sjes.elasticsearch.feigns.category.model.Category;

import java.util.List;
import java.util.Map;

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
        Category category = new Category();
        category.setDisplay(true);
        return categoryFeign.list(category);
    }

    /**
     * 更新分类下的商品数目
     * @param categoryProductNumMap 商品分类Id和商品数目
     * @return
     */
    public ResponseMessage updateProductNum(Map<Long, Integer> categoryProductNumMap) {
        return categoryFeign.updateProductNum(categoryProductNumMap);
    }

    /**
     * 更新分类下的商品数目Axsh
     * @param categoryProductNumMap 商品分类Id和商品数目
     * @return
     */
    public ResponseMessage updateProductNumAxsh( String shopId, Map<Long, Integer> categoryProductNumMap) {
        return categoryFeign.updateProductNumAxsh(shopId,categoryProductNumMap);
    }

}
