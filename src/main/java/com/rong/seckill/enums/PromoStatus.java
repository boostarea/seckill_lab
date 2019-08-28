package com.rong.seckill.enums;


/**
 * @Description 秒杀活动状态
 * @Author chenrong
 * @Date 2019-08-28 11:31
 **/
public enum PromoStatus {
    PRE(1, "未开始"),
    DOING(2, "进行中"),
    FINISHED(3, "已结束");

    private Integer code;
    private String name;

    PromoStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
