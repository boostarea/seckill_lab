package com.rong.seckill.infrastructure.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class UserPassword {
    @Id
    private Integer id;
    private String encrptPassword;
    private Integer userId;
}