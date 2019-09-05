package com.rong.seckill.domain.service;


import com.rong.seckill.domain.model.PromoModel;
import com.rong.seckill.infrastructure.response.error.BusinessException;

/**
 * @Author chenrong
 * @Date 2019-08-28 20:27
 **/
public interface PromoService {
    //根据itemid获取即将进行的或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);

    /**
     * 发布活动
     * @param promoId
     * @throws BusinessException
     */
    void publishPromotion(Integer promoId) throws BusinessException;

    /**
     * 生成秒杀令牌
     * @param promoId
     * @param itemId
     * @param userId
     * @return
     * @throws BusinessException
     */
    String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId) throws BusinessException;
}
