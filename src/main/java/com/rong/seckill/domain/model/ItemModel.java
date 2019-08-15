package com.rong.seckill.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author chenrong
 * @Date 2019-08-11 15:27
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemModel implements Serializable{
    private Integer id;

    @NotBlank(message = "商品名称不能为空")
    private String title;

    @NotNull(message = "商品价格不能为空")
    @Min(value = 0,message = "商品价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "库存不能不填")
    private Integer stock;

    @NotBlank(message = "商品描述信息不能为空")
    private String description;

    //销量
    private Integer sales;

    @NotBlank(message = "商品图片信息不能为空")
    private String imgUrl;

    //使用聚合模型,如果promoModel不为空，则表示其拥有还未结束的秒杀活动
    private PromoModel promoModel;
}
