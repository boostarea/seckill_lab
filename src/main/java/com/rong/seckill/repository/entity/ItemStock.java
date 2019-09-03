package com.rong.seckill.repository.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class ItemStock {
    @Id
    private Integer id;
    private Integer stock;
    private Integer itemId;
}