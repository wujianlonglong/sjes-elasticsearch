package sjes.elasticsearch.domainaxsh;

import lombok.Data;
import sjes.elasticsearch.feigns.category.model.Category;

import java.util.List;

/**
 * Created by qinhailong on 15-12-3.
 */
@Data
public class CategoryIndexAxsh extends Category {

    // 商品列表
    private List<ProductIndexAxsh> productIndexAxshes;

}
