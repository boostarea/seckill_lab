package com.rong.seckill.infrastructure.web;

import com.rong.seckill.infrastructure.web.vo.ItemVO;
import com.rong.seckill.domain.model.ItemModel;
import com.rong.seckill.domain.service.ItemService;
import com.rong.seckill.domain.service.PromoService;
import com.rong.seckill.infrastructure.response.error.BusinessException;
import com.rong.seckill.infrastructure.response.CommonReturnType;
import com.rong.seckill.util.convertor.ItemConvertor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author chenrong
 * @Date 2019-08-28 15:27
 **/
@RestController
@RequestMapping("item")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class ItemController extends BaseController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private PromoService promoService;

    @PostMapping(value = "create", consumes={CONTENT_TYPE_FORMED})
    public CommonReturnType createItem(@RequestParam(name = "title")String title,
                                       @RequestParam(name = "description")String description,
                                       @RequestParam(name = "price")BigDecimal price,
                                       @RequestParam(name = "stock")Integer stock,
                                       @RequestParam(name = "imgUrl")String imgUrl) throws BusinessException {
        ItemModel itemModel = ItemModel.builder()
                .title(title)
                .description(description)
                .price(price)
                .stock(stock)
                .imgUrl(imgUrl).build();
        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        return CommonReturnType.create(ItemConvertor.convertVOFromModel(itemModelForReturn));
    }

    @GetMapping("publish/{id}")
    public CommonReturnType publishPromotion(@PathVariable Integer id) throws BusinessException {
        promoService.publishPromotion(id);
        return CommonReturnType.create(null);
    }

    @GetMapping("get/{id}")
    public CommonReturnType get(@PathVariable Integer id) throws BusinessException {
        ItemModel model = itemService.getItem(id);
        ItemVO itemVO = ItemConvertor.convertVOFromModel(model);
        return CommonReturnType.create(itemVO);
    }

    @GetMapping("list")
    public CommonReturnType listItem(){
        List<ItemVO> itemVOList =  itemService.listItem()
                .stream()
                .map(ItemConvertor::convertVOFromModel)
                .collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }

}
