package com.rong.seckill.domain.service.impl;

import com.rong.seckill.domain.model.ItemModel;
import com.rong.seckill.domain.model.PromoModel;
import com.rong.seckill.domain.service.CacheService;
import com.rong.seckill.domain.service.ItemService;
import com.rong.seckill.domain.service.PromoService;
import com.rong.seckill.repository.entity.Item;
import com.rong.seckill.repository.entity.ItemStock;
import com.rong.seckill.repository.entity.StockLog;
import com.rong.seckill.infrastructure.enums.PromoStatus;
import com.rong.seckill.infrastructure.response.error.BusinessException;
import com.rong.seckill.infrastructure.response.error.EmBusinessError;
import com.rong.seckill.infrastructure.mq.MqProducer;
import com.rong.seckill.repository.ItemRepository;
import com.rong.seckill.repository.ItemStockRepository;
import com.rong.seckill.repository.StockLogRepository;
import com.rong.seckill.util.convertor.ItemConvertor;
import com.rong.seckill.util.validator.ValidationResult;
import com.rong.seckill.util.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.rong.seckill.util.validator.Validator.isNull;

/**
 * @Author chenrong
 * @Date 2019-08-28 15:27
 **/
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemStockRepository itemStockRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StockLogRepository stockLogRepository;

    @Autowired
    private PromoService promoService;

    @Autowired
    private CacheService cacheService;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        ValidationResult result = validator.validate(itemModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }
        Item item = ItemConvertor.convertItemDOFromItemModel(itemModel);
        itemRepository.save(item);
        itemModel.setId(item.getId());

        ItemStock itemStock = ItemConvertor.convertItemStockDOFromItemModel(itemModel);
        itemStockRepository.save(itemStock);
        return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        return itemRepository.listItem()
                .stream()
                .map(itemDO -> {
                    ItemStock itemStockDO = itemStockRepository.findByItemId(itemDO.getId());
                    return this.convertModelFromDataObject(itemDO, itemStockDO);
                }).collect(Collectors.toList());
    }

    @Override
    public ItemModel getItem(Integer id) throws BusinessException {
        //1. 取本地缓存
        ItemModel itemModel = (ItemModel) cacheService.getFromCommonCache("item_"+id);

        if(isNull(itemModel)){
            //2. Redis内获取
            itemModel = (ItemModel) redisTemplate.opsForValue().get("item_"+id);
            //3. DB
            if(isNull(itemModel)){
                itemModel = this.getItemById(id);
                redisTemplate.opsForValue().set("item_"+id, itemModel);
                redisTemplate.expire("item_"+id,10, TimeUnit.MINUTES);
            }
            //填充本地缓存
            cacheService.setCommonCache("item_"+id, itemModel);
        }
        return itemModel;
    }

    @Override
    public ItemModel getItemById(Integer id) throws BusinessException {
        Item item = itemRepository.getOne(id);
        if(isNull(item)){
            throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST);
        }
        //获得库存数量
        ItemStock itemStockDO = itemStockRepository.findByItemId(item.getId());
        ItemModel itemModel = convertModelFromDataObject(item,itemStockDO);

        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if(!isNull(promoModel) && !PromoStatus.FINISHED.getCode().equals(promoModel.getStatus())){
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    // 用户风控模型
    @Override
    public ItemModel getItemByIdInCache(Integer id) throws BusinessException {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_" + id);
        if(isNull(itemModel)){
            itemModel = this.getItemById(id);
            redisTemplate.opsForValue().set("item_validate_" + id, itemModel);
            redisTemplate.expire("item_validate_"+id,10, TimeUnit.MINUTES);
        }
        return itemModel;
    }


    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        //int affectedRow =  itemStockDOMapper.decreaseStock(itemId,amount);
        long result = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount * -1);
        if(result > 0){
            //更新库存成功
            return true;
        }else if(result == 0){
            //打上库存已售罄的标识
            redisTemplate.opsForValue().set("promo_item_stock_invalid_" + itemId, "true");
            return true;
        }else{
            //更新库存失败
            increaseStock(itemId,amount);
            return false;
        }
    }

    @Override
    public boolean increaseStock(Integer itemId, Integer amount) throws BusinessException {
        redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount);
        return true;
    }

    @Override
    public boolean asyncDecreaseStock(Integer itemId, Integer amount) {
        boolean mqResult = mqProducer.asyncReduceStock(itemId, amount);
        return mqResult;
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemRepository.increaseSales(itemId, amount);
    }

    //初始化对应的库存流水
    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {
        StockLog stockLog = new StockLog();
        stockLog.setItemId(itemId);
        stockLog.setAmount(amount);
        stockLog.setStockLogId(UUID.randomUUID().toString().replace("-",""));
        stockLog.setStatus(1);

        stockLogRepository.save(stockLog);
        return stockLog.getStockLogId();
    }

    private ItemModel convertModelFromDataObject(Item itemDO,ItemStock itemStockDO){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }

}
