package com.rong.seckill.infrastructure.web.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author chenrong
 * @Date 2019-08-28 20:27
 **/
@Data
public class ItemVO {
    private Integer id;
    //名称
    private String title;
    //价格
    private BigDecimal price;
    //库存
    private Integer stock;
    //描述
    private String description;
    //销量
    private Integer sales;
    //描述图片的url
    private String imgUrl;
    //记录商品是否在秒杀活动中，以及对应的状态0：表示没有秒杀活动，1表示秒杀活动待开始，2表示秒杀活动进行中
    private Integer promoStatus;
    //秒杀活动价格
    private BigDecimal promoPrice;
    //秒杀活动ID
    private Integer promoId;
    //秒杀活动开始时间
    private String startDate;
}
