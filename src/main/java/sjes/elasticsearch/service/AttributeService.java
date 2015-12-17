package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.feigns.category.feign.AttributeFeign;
import sjes.elasticsearch.feigns.category.model.AttributeModel;
import sjes.elasticsearch.utils.ListUtils;

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
        List<AttributeModel> attributeModels = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(categoryIds)) {
            List<List<Long>> categoryIdsList = ListUtils.splitList(categoryIds, Constants.SPLIT_SUB_LIST_SIZE);
            for (List<Long> cateIds : categoryIdsList) {
                attributeModels.addAll(attributeFeign.lists(cateIds));
            }
        }
        return attributeModels;
    }

}
