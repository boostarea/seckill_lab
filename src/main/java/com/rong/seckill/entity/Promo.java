package com.rong.seckill.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "promo")
public class Promo  {
    @Id
    private Integer id;
    private String promoName;
    private Date startDate;
    private Date endDate;
    private Integer itemId;
    private Double promoItemPrice;
}