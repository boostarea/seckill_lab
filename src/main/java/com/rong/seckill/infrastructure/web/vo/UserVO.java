package com.rong.seckill.infrastructure.web.vo;

import lombok.Data;

/**
 * @Author chenrong
 * @Date 2019-08-28 15:27
 **/
@Data
public class UserVO {
    private Integer id;
    private String name;
    private Byte gender;
    private Integer age;
    private String telphone;
}
