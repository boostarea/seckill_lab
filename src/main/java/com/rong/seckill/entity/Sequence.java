package com.rong.seckill.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "sequence_info")
public class Sequence {
    @Id
    private String name;
    private Integer currentValue;
    private Integer step;
}