package com.rong.seckill.domain.service;

import com.rong.seckill.domain.model.OrderModel;
import com.rong.seckill.domain.model.UserModel;
import com.rong.seckill.error.BusinessException;

/**
 * @Author chenrong
 * @Date 2019-08-11 15:27
 **/
public interface OrderService {
    void create(Integer itemId, Integer amount, Integer promoId, String promoToken, UserModel userModel) throws BusinessException;

    //使用1,通过前端url上传过来秒杀活动id，然后下单接口内校验对应id是否属于对应商品且活动已开始
    //2.直接在下单接口内判断对应的商品是否存在秒杀活动，若存在进行中的则以秒杀价格下单
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws BusinessException;

    String generateToken(Integer itemId, Integer promoId, UserModel userModel) throws BusinessException;
}
