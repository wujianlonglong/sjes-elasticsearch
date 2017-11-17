package sjes.elasticsearch.feigns.sale.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by kimiyu on 17/1/6.
 */
@Data
public class PromotionDTO<T> extends BaseParam implements Serializable {

    private T data;
}
