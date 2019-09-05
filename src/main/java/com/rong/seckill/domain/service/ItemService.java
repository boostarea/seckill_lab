package com.rong.seckill.domain.service;

import com.rong.seckill.domain.model.ItemModel;
import com.rong.seckill.infrastructure.response.error.BusinessException;

import java.util.List;

/**
 * @Author chenrong
 * @Date 2019-08-28 20:27
 **/
public interface ItemService {

    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    List<ItemModel> listItem();

    ItemModel getItem(Integer id) throws BusinessException;

    ItemModel getItemById(Integer id) throws BusinessException;

    ItemModel getItemByIdInCache(Integer id) throws BusinessException;

    /**
     * 库存扣减
     * @param itemId
     * @param amount
     * @return
     * @throws BusinessException
     */
    boolean decreaseStock(Integer itemId, Integer amount)throws BusinessException;

    /**
     * 库存回补
     * @param itemId
     * @param amount
     * @return
     * @throws BusinessException
     */
    boolean increaseStock(Integer itemId, Integer amount)throws BusinessException;

    /**
     * 异步更新库存
     * @param itemId
     * @param amount
     * @return
     */
    boolean asyncDecreaseStock(Integer itemId, Integer amount);

    /**
     * 增加商品销量
     * @param itemId
     * @param amount
     * @throws BusinessException
     */
    void increaseSales(Integer itemId, Integer amount)throws BusinessException;

    /**
     * 初始化库存流水
     * @param itemId
     * @param amount
     * @return
     */
    String initStockLog(Integer itemId, Integer amount);

}
