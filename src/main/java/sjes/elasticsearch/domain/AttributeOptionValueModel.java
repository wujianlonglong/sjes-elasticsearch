package sjes.elasticsearch.domain;

import lombok.Data;
import sjes.elasticsearch.feigns.category.model.Attribute;
import sjes.elasticsearch.feigns.category.model.AttributeOption;

/**
 * Created by qinhailong on 15-12-8.
 */
@Data
public class AttributeOptionValueModel extends Attribute {

    private AttributeOption attributeOption;
}
