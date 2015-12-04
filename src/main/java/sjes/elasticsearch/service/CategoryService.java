package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.domain.Category;
import sjes.elasticsearch.domain.CategoryIndex;
import sjes.elasticsearch.feigns.category.feign.CategoryFeign;
import sjes.elasticsearch.feigns.constants.CategoryConstant;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Service("categoryService")
public class CategoryService {

    @Autowired
    private CategoryFeign categoryFeign;


    /**
     * 得到分类索引文档列表
     * @return 分类索引文档列表
     */
    public List<CategoryIndex> getCategoryIndexs() {
        List<Category> categories = categoryFeign.findByGrade(CategoryConstant.CategoryGradeConstants.GRADE_THREE);
        List<CategoryIndex> categoryIndexes = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(categories)) {
            categories.forEach(category -> {
              // TODO
            });
        }
        return categoryIndexes;
    }

}
