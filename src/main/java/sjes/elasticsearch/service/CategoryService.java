package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.constants.Constants;
import sjes.elasticsearch.domain.CategoryIndex;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.domain.ProductIndexModel;
import sjes.elasticsearch.feigns.category.feign.CategoryFeign;
import sjes.elasticsearch.feigns.category.model.AttributeModel;
import sjes.elasticsearch.feigns.category.model.AttributeOption;
import sjes.elasticsearch.feigns.category.model.Category;
import sjes.elasticsearch.feigns.category.model.Tag;
import sjes.elasticsearch.feigns.item.model.ProductAttributeValue;
import sjes.elasticsearch.feigns.item.model.ProductTag;

import java.util.List;
import java.util.Map;

/**
 * Created by qinhailong on 15-12-4.
 */
@Service("categoryService")
public class CategoryService {

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductAttributeValueService productAttributeValueService;

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private TagService tagService;


    /**
     * 得到分类索引文档列表
     * @return 分类索引文档列表
     */
    public List<CategoryIndex> getCategoryIndexs() {
        List<Category> categories = categoryFeign.findByGrade(Constants.CategoryGradeConstants.GRADE_THREE);
        Map<Long, CategoryIndex> categoryIndexMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(categories)) {
            categories.forEach(category -> {
                CategoryIndex categoryIndex = new CategoryIndex();
                categoryIndex.setProductIndexes(Lists.newArrayList());
                BeanUtils.copyProperties(category, categoryIndex);
                categoryIndexMap.put(category.getId(), categoryIndex);
            });
            List<Long> categoryIds = Lists.newArrayList(categoryIndexMap.keySet());
            List<ProductIndexModel> productIndexModels = productService.listByCategoryIds(categoryIds);
            List<AttributeModel> attributeModels = attributeService.lists(categoryIds);
            Map<Long, String> attributeNameMaps = Maps.newHashMap();
            Map<Long, String> attributeOptionValueMaps = Maps.newHashMap();
            List<Tag> tags = tagService.all();
            Map<Long, Tag> tagMap = Maps.newHashMapWithExpectedSize(tags.size());
            if (CollectionUtils.isNotEmpty(tags)) {
                tags.forEach(tag -> {
                    tagMap.put(tag.getId(), tag);
                });
            }
            if (CollectionUtils.isNotEmpty(attributeModels)) {
                attributeModels.forEach(attributeModel -> {
                    attributeNameMaps.put(attributeModel.getId(), attributeModel.getName());
                    List<AttributeOption> attributeOptions = attributeModel.getAttributeOptions();
                    if (CollectionUtils.isNotEmpty(attributeOptions)) {
                        attributeOptions.forEach(attributeOption -> {
                            attributeOptionValueMaps.put(attributeOption.getId(), attributeOption.getValue());
                        });
                    }
                });
            }
            Map<Long, ProductIndex> productMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(productIndexModels)) {
                productIndexModels.forEach(productIndexModel -> {
                    ProductIndex productIndex = new ProductIndex();
                    productIndex.setTags(Lists.newArrayList());
                    productIndex.setProductAttributeValues(Lists.newArrayList());
                    BeanUtils.copyProperties(productIndexModel, productIndex);
                    List<ProductTag> productTags = productIndex.getProductTags();
                    if (CollectionUtils.isNotEmpty(productTags)) {
                        productTags.forEach(productTag -> {
                            productIndex.getTags().add(tagMap.get(productTag.getTagId()));
                        });
                    }
                    categoryIndexMap.get(productIndexModel.getCategoryId()).getProductIndexes().add(productIndex);
                    productMap.put(productIndexModel.getId(), productIndex);
                });
            }
            List<ProductAttributeValue> productAttributeValues = productAttributeValueService.listByProductIds(Lists.newArrayList(productMap.keySet()));
            if (CollectionUtils.isNotEmpty(productAttributeValues)) {
                productAttributeValues.forEach(productAttributeValue -> {
                    productAttributeValue.setAttributeName(attributeNameMaps.get(productAttributeValue.getAttributeId()));
                    productAttributeValue.setAttributeOptionName(attributeOptionValueMaps.get(productAttributeValue.getAttributeOptionId()));
                    productMap.get(productAttributeValue.getProductId()).getProductAttributeValues().add(productAttributeValue);
                });
            }
        }
        return Lists.newArrayList(categoryIndexMap.values());
    }

}
