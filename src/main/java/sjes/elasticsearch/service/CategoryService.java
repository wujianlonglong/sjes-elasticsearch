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
import sjes.elasticsearch.feigns.category.feign.CategoryFeign;
import sjes.elasticsearch.feigns.category.model.AttributeModel;
import sjes.elasticsearch.feigns.category.model.AttributeOption;
import sjes.elasticsearch.feigns.category.model.Category;
import sjes.elasticsearch.feigns.item.model.Product;
import sjes.elasticsearch.feigns.item.model.ProductAttributeValue;

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
            List<Product> products = productService.listByCategoryIds(categoryIds);
            List<AttributeModel> attributeModels = attributeService.lists(categoryIds);
            Map<Long, String> attributeNameMaps = Maps.newHashMap();
            Map<Long, String> attributeOptionValueMaps = Maps.newHashMap();
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
            if (CollectionUtils.isNotEmpty(products)) {
                products.forEach(product -> {
                    ProductIndex productIndex = new ProductIndex();
                    productIndex.setProductAttributeValues(Lists.newArrayList());
                    BeanUtils.copyProperties(product, productIndex);
                    categoryIndexMap.get(product.getCategoryId()).getProductIndexes().add(productIndex);
                    productMap.put(product.getId(), productIndex);
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
