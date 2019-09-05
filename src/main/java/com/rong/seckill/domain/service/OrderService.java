package com.rong.seckill.domain.service;

import com.rong.seckill.domain.model.OrderModel;
import com.rong.seckill.domain.model.UserModel;
import com.rong.seckill.infrastructure.response.error.BusinessException;

/**
 * @Author chenrong
 * @Date 2019-08-28 20:27
 **/
public interface OrderService {

    void create(Integer itemId, Integer amount, Integer promoId, String promoToken, UserModel userModel) throws BusinessException;

    //1. 校验对应活动已开始
    //2. 判断对应的商品是否存在秒杀活动，若存在进行中的则以秒杀价格下单
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws BusinessException;

    /**
     * 生成秒杀令牌
     * @param itemId
     * @param promoId
     * @param userModel
     * @return
     * @throws BusinessException
     */
    String generateToken(Integer itemId, Integer promoId, UserModel userModel) throws BusinessException;
}
