package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.CategoryIndex;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.feigns.category.feign.CategoryBasicAttributesFeign;
import sjes.elasticsearch.feigns.category.model.CategoryBasicAttribute;
import sjes.elasticsearch.feigns.category.model.CategoryBasicAttributeOption;
import sjes.elasticsearch.feigns.category.model.CategoryBasicAttributesModel;
import sjes.elasticsearch.feigns.category.model.NameValueModel;
import sjes.elasticsearch.feigns.item.model.ProductAttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by qinhailong on 15-12-7.
 */
@Service("categoryBasicAttributes")
public class CategoryBasicAttributesService {

    @Autowired
    private CategoryBasicAttributesFeign categoryBasicAttributesFeign;

    /**
     * 新增或修改分类查询属性列表
     * @param categoryIndexes 分类查询属性列表
     * @return 是否成功
     */
    public void saveOrUpdate(List<CategoryIndex> categoryIndexes) throws ServiceException {
        if (CollectionUtils.isNotEmpty(categoryIndexes)) {
            List<CategoryBasicAttributesModel> categoryBasicAttributesModelList = Lists.newArrayListWithCapacity(categoryIndexes.size());
            categoryIndexes.forEach(categoryIndex -> {
                CategoryBasicAttributesModel categoryBasicAttributesModel = new CategoryBasicAttributesModel();
                categoryBasicAttributesModel.setCategoryId(categoryIndex.getId());
                categoryBasicAttributesModel.setCategoryAttributes(Lists.newArrayList());
                List<ProductIndex> productIndexes = categoryIndex.getProductIndexes();
                if (CollectionUtils.isNotEmpty(productIndexes)) {
                    Set<String> brandNameSet = Sets.newHashSet();
                    Set<String> placeSet = Sets.newHashSet();
                    List<NameValueModel> nameValueModels = Lists.newArrayList();
                    Map<String, CategoryBasicAttribute> attributeNameMap = Maps.newHashMap();
                    Map<String, Set<String>> attributeOptionNamesMap = Maps.newHashMap();
                    productIndexes.forEach(productIndex -> {  // 添加分类品牌信息
                        String brandName = StringUtils.trim(productIndex.getBrandName());
                        String place = StringUtils.trim(productIndex.getPlace());
                        if (null != brandName && brandNameSet.add(brandName)) {
                            NameValueModel nameValueModel = new NameValueModel();
                            nameValueModel.setName(brandName);
                            Long brandId = productIndex.getBrandId();
                            nameValueModel.setValue(null == brandId ? "" : brandId.toString());
                            nameValueModels.add(nameValueModel);
                        }
                        if (null != place) {
                            placeSet.add(place); // 添加分类产地
                        }
                        List<ProductAttributeValue> productAttributeValues = productIndex.getProductAttributeValues();
                        if (CollectionUtils.isNotEmpty(productAttributeValues)) { // 添加分类属性
                            productAttributeValues.forEach(productAttributeValue -> {
                                CategoryBasicAttribute categoryBasicAttribute = null;
                                String attributeName = StringUtils.trim(productAttributeValue.getAttributeName());
                                String attributeOptionName = StringUtils.trim(productAttributeValue.getAttributeOptionName());
                                if (!attributeNameMap.containsKey(attributeName)) {
                                    categoryBasicAttribute = new CategoryBasicAttribute();
                                    categoryBasicAttribute.setAttributeName(attributeName);
                                    categoryBasicAttribute.setAttributeId(productAttributeValue.getAttributeId());
                                    CategoryBasicAttributeOption categoryBasicAttributeOption = new CategoryBasicAttributeOption();
                                    categoryBasicAttributeOption.setAttributeOptionId(productAttributeValue.getAttributeOptionId());
                                    categoryBasicAttributeOption.setAttributeOptionName(attributeOptionName);
                                    categoryBasicAttribute.setCategoryAttributeOptions(Lists.newArrayList(categoryBasicAttributeOption));
                                    attributeNameMap.put(attributeName, categoryBasicAttribute);
                                    attributeOptionNamesMap.put(attributeName, Sets.newHashSet(attributeOptionName));
                                    categoryBasicAttributesModel.getCategoryAttributes().add(categoryBasicAttribute);
                                }
                                else {
                                    categoryBasicAttribute = attributeNameMap.get(attributeName);
                                    if (null != categoryBasicAttribute) {
                                        Set<String> attributeOptionNames = (Set<String>) MapUtils.getObject(attributeOptionNamesMap, attributeName, Sets.newHashSet());
                                        if (attributeOptionNames.add(attributeOptionName)) {
                                            CategoryBasicAttributeOption categoryBasicAttributeOption = new CategoryBasicAttributeOption();
                                            categoryBasicAttributeOption.setAttributeOptionId(productAttributeValue.getAttributeOptionId());
                                            categoryBasicAttributeOption.setAttributeOptionName(attributeOptionName);
                                            categoryBasicAttribute.getCategoryAttributeOptions().add(categoryBasicAttributeOption);
                                        }
                                    }
                                }
                            });
                        }
                    });
                    categoryBasicAttributesModel.setBrands(nameValueModels);
                    categoryBasicAttributesModel.setPlaces(Lists.newArrayList(placeSet));
                }
                categoryBasicAttributesModelList.add(categoryBasicAttributesModel);
            });
            if (CollectionUtils.isNotEmpty(categoryBasicAttributesModelList)) {
                categoryBasicAttributesFeign.saveOrUpdate(categoryBasicAttributesModelList);
            }
        }


    }
}
