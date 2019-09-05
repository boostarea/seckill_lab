package com.rong.seckill.domain.service.impl;

import com.rong.seckill.domain.model.ItemModel;
import com.rong.seckill.domain.model.PromoModel;
import com.rong.seckill.domain.model.UserModel;
import com.rong.seckill.domain.service.ItemService;
import com.rong.seckill.domain.service.PromoService;
import com.rong.seckill.domain.service.UserService;
import com.rong.seckill.repository.entity.Promo;
import com.rong.seckill.infrastructure.enums.PromoStatus;
import com.rong.seckill.infrastructure.response.error.BusinessException;
import com.rong.seckill.repository.PromoRepository;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.rong.seckill.util.validator.Validator.*;

/**
 * @Author chenrong
 * @Date 2019-08-28 20:27
 **/
@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoRepository promoRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        //获取对应商品的秒杀活动信息
        Promo promo = promoRepository.findByItemId(itemId);
        PromoModel promoModel = getPromoModel(promo);
        if (isNull(promoModel)) {
            return null;
        }
        return promoModel;
    }

    @Override
    public void publishPromotion(Integer promoId) throws BusinessException {
        //获取活动
        Promo promo = promoRepository.getOne(promoId);
        if(isNull(promo) || isNull(promo.getItemId()) || promo.getItemId() == 0){
            return;
        }
        ItemModel itemModel = itemService.getItemById(promo.getItemId());
        //todo 活动开始，自动上架商品，避免扣减非活动库存

        //库存同步到redis
        redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(), itemModel.getStock());
        //大闸的限制到redis内 todo 基数应该放到配置中心
        redisTemplate.opsForValue().set("promo_door_count_"+promoId, itemModel.getStock() * 5);
    }

    @Override
    public String generateSecondKillToken(Integer promoId,Integer itemId,Integer userId) throws BusinessException {
        //是否售罄
        if(redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)){
            return null;
        }

        //活动是否正在进行
        Promo promo = promoRepository.getOne(promoId);
        if (isNull(promo)) {
            return null;
        }
        PromoModel promoModel = getPromoModel(promo);
        if(!PromoStatus.DOING.getCode().equals(promoModel.getStatus())){
            return null;
        }
        //商品是否存在 移到cache
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(isNull(itemModel)){
            return null;
        }
        //用户是否存在
        UserModel userModel = userService.getUserByIdInCache(userId);
        if(isNull(userModel)){
            return null;
        }
        //是否通过秒杀大闸
        long result = redisTemplate.opsForValue().increment("promo_door_count_" + promoId,-1);
        if(result < 0){
            return null;
        }
        //生成token
        String token = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set("promo_token_" + promoId + "_userid_" + userId + "_itemid_" + itemId, token);
        redisTemplate.expire("promo_token_" + promoId + "_userid_" + userId + "_itemid_" + itemId,5, TimeUnit.MINUTES);
        return token;
    }

    private PromoModel getPromoModel(Promo promo) {
        PromoModel promoModel = convertFromDataObject(promo);
        if (promoModel == null) {
            return null;
        }

        //判断秒杀活动即将开始或正在进行
        if (promoModel.getStartDate().isAfterNow()) {
            promoModel.setStatus(PromoStatus.PRE.getCode());
        } else if (promoModel.getEndDate().isBeforeNow()) {
            promoModel.setStatus(PromoStatus.FINISHED.getCode());
        } else {
            promoModel.setStatus(PromoStatus.DOING.getCode());
        }
        return promoModel;
    }

    private PromoModel convertFromDataObject(Promo promo){
        if(promo == null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promo,promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promo.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promo.getStartDate()));
        promoModel.setEndDate(new DateTime(promo.getEndDate()));
        return promoModel;
    }
}
