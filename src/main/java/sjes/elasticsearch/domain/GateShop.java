package sjes.elasticsearch.domain;

import lombok.Data;

@Data
public class GateShop {


    private String id;

    /***
     * 经度
     */
    private Double longitude;

    /***
     * 纬度
     */
    private Double latitude;

    /***
     * 区域
     */
    private String areaName;

    /***
     * 门店编号
     */
    private String shopId;

    /***
     * 门店名称
     */
    private String shopName;

    /***
     * 配送方
     */
    private Integer distributWay;

    /***
     * 运费
     */
    private String distributCost;

    /***
     * 门店地址
     */
    private String address;

    /***
     * 配送距离
     */
    private String distance;

    /***
     * 开店时间
     */
    private String openTime;

    /***
     * 闭店时间
     */
    private String closeTime;
}
