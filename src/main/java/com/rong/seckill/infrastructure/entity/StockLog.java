package com.rong.seckill.infrastructure.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class StockLog {
    @Id
    private String stockLogId;
    private Integer itemId;
    private Integer amount;
    private Integer status;
}