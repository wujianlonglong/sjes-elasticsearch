package sjes.elasticsearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.feigns.category.feign.AttributeFeign;
import sjes.elasticsearch.feigns.category.model.AttributeModel;

import java.util.List;

/**
 * Created by qinhailong on 15-12-4.
 */
@Service("attributeService")
public class AttributeService {

    @Autowired
    private AttributeFeign attributeFeign;

    /**
     * 根据分类ids查询AttributeModel列表
     * @param categoryIds 分类ids
     * @return AttributeModel列表
     */
    public List<AttributeModel> lists(List<Long> categoryIds) {
        return attributeFeign.lists(categoryIds);
    }

}
