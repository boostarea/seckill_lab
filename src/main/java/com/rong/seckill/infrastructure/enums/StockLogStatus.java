package com.rong.seckill.infrastructure.enums;


/**
 * @Description 库存日志状态
 * @Author chenrong
 * @Date 2019-08-28 11:31
 **/
public enum StockLogStatus {
    INIT(1, "初始状态"),
    SUCCESS(2, "扣减库存成功"),
    ROLLBACK(3, "下单回滚");

    private Integer code;
    private String name;

    StockLogStatus(Integer code, String name) {
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
