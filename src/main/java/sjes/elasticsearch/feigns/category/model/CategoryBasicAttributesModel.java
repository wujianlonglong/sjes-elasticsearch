package sjes.elasticsearch.feigns.category.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分类属性
 * Created by mac on 15/9/18.
 */
@Data
public class CategoryBasicAttributesModel implements Serializable {

    private Long categoryId; // 分类Id

    private List<NameValueModel> brands; // 品牌(name为品牌名称, value为品牌Id)

    private List<String> places; // 产地

    private List<CategoryBasicAttribute> categoryAttributes; // 分类属性项
}
