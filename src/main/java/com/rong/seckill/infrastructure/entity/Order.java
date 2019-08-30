package com.rong.seckill.infrastructure.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "order_info")
public class Order {
    @Id
    private String id;
    private Integer userId;
    private Integer itemId;
    private Double itemPrice;
    private Integer amount;
    private Double orderPrice;
    private Integer promoId;
}