package com.rong.seckill.util.convertor;

import com.rong.seckill.infrastructure.web.vo.ItemVO;
import com.rong.seckill.domain.model.ItemModel;
import com.rong.seckill.domain.model.PromoModel;
import com.rong.seckill.repository.entity.Item;
import com.rong.seckill.repository.entity.ItemStock;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;

import java.util.Optional;

/**
 * @Description TODO
 * @Author chenrong
 * @Date 2019-08-27 21:00
 **/
public class ItemConvertor {

    public static ItemVO convertVOFromModel(ItemModel itemModel) {
        if (!Optional.ofNullable(itemModel).isPresent()) {
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);

        Optional<PromoModel> promoOpt = Optional.ofNullable(itemModel.getPromoModel());
        if (promoOpt.isPresent()) {
            //有正在进行或即将进行的秒杀活动
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
        } else {
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }

    public static Item convertItemDOFromItemModel(ItemModel itemModel) {
        if (!Optional.ofNullable(itemModel).isPresent()) {
            return null;
        }
        Item item = new Item();
        BeanUtils.copyProperties(itemModel,item);
        item.setPrice(itemModel.getPrice().doubleValue());
        return item;
    }

    public static ItemStock convertItemStockDOFromItemModel(ItemModel itemModel) {
        if (!Optional.ofNullable(itemModel).isPresent()) {
            return null;
        }
        ItemStock itemStock = new ItemStock();
        itemStock.setItemId(itemModel.getId());
        itemStock.setStock(itemModel.getStock());
        return itemStock;
    }
}
