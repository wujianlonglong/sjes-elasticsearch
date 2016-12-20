package sjes.elasticsearch.feigns.item.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import sjes.elasticsearch.serializer.MoneySerializer;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by qinhailong on 05/12/2016.
 */
@Data
public class ItemPrice implements Serializable {

    private Long id; // 主键

    private Long erpGoodsId; // 对应ERP GoodsID

    private String shopId; // 门店id

    @JsonSerialize(using = MoneySerializer.class)
    @Field(type = FieldType.Double)
    private BigDecimal salePrice; // 销售价

    @JsonSerialize(using = MoneySerializer.class)
    @Field(type = FieldType.Double)
    private BigDecimal memberPrice; // 会员价

}
