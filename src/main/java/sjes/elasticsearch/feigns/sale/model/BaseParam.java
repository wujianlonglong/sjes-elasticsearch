package sjes.elasticsearch.feigns.sale.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by kimiyu on 16/12/23.
 */
@Data
public class BaseParam implements Serializable {

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 门店编号
     */
    private String shopId;

    /**
     * 环境变量
     */
    private Integer env;

    private Integer page;

    private Integer size;
}
