package sjes.elasticsearch.feigns.item.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by qinhailong on 15/8/28.
 */
@Data
public class ProductImage implements Serializable {

    private Long id; // 主键

    private Long erpGoodsId; // 对应ERP 对应ERP GLBH

    private Boolean isDefault; // 是否是默认图片

    private Integer orders; // 排序

    private String source;  // 原图

    private String title;   // 标题

    private String large800; // 主图等比大图

    private String medium400;// 详情页主图大图400

    private String thumbnail220; // 缩略图

    private String thumbnail100; // 广告位(首页、分类、详情页、搜索页)

    private String thumbnail50;  // 详情页主图小图、购物车
}
