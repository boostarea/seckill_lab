package com.rong.seckill.domain.service;


import com.rong.seckill.domain.model.PromoModel;

/**
 * @Author chenrong
 * @Date 2019-08-11 15:27
 **/
public interface PromoService {
    //根据itemid获取即将进行的或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);

    //活动发布
    void publishPromo(Integer promoId);

    //生成秒杀用的令牌
    String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId);
}
