package com.rong.seckill.domain.service;

/**
 * @Description 封装本地缓存操作类
 * @Author chenrong
 * @Date 2019-08-11 15:27
 **/
public interface CacheService {
    //存方法
    void setCommonCache(String key, Object value);

    //取方法
    Object getFromCommonCache(String key);
}
