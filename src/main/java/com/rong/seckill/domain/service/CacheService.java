package com.rong.seckill.domain.service;

/**
 * @Description 封装本地缓存操作类
 * @Author chenrong
 * @Date 2019-08-28 15:27
 **/
public interface CacheService {
    void setCommonCache(String key, Object value);

    Object getFromCommonCache(String key);
}
