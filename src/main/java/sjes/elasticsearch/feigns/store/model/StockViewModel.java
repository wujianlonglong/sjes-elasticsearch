package sjes.elasticsearch.feigns.store.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by kimiyu on 16/6/27.
 */
@Data
public class StockViewModel implements Serializable {

    private String shopId;

    private List<Long> goodsIdList;
}
