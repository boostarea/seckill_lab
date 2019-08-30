package com.rong.seckill.infrastructure.entity;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class Promo  {
    @Id
    private Integer id;
    private String promoName;
    private Date startDate;
    private Date endDate;
    private Integer itemId;
    private Double promoItemPrice;
}