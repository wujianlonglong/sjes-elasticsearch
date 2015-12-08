package sjes.elasticsearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.constants.Constants;
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
     * 得到分类索引文档列表
     * @return 分类索引文档列表
     */
    public List<Category> listByGradeThree() {
        return categoryFeign.findByGrade(Constants.CategoryGradeConstants.GRADE_THREE);
    }

}
