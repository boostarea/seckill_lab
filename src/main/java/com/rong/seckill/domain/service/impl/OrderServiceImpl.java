package com.rong.seckill.domain.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import com.rong.seckill.domain.model.ItemModel;
import com.rong.seckill.domain.model.OrderModel;
import com.rong.seckill.domain.model.UserModel;
import com.rong.seckill.domain.service.ItemService;
import com.rong.seckill.domain.service.OrderService;
import com.rong.seckill.domain.service.PromoService;
import com.rong.seckill.infrastructure.enums.StockLogStatus;
import com.rong.seckill.repository.entity.Order;
import com.rong.seckill.repository.entity.Sequence;
import com.rong.seckill.repository.entity.StockLog;
import com.rong.seckill.infrastructure.response.error.BusinessException;
import com.rong.seckill.infrastructure.response.error.EmBusinessError;
import com.rong.seckill.infrastructure.mq.MqProducer;
import com.rong.seckill.repository.OrderRepository;
import com.rong.seckill.repository.SequenceRepository;
import com.rong.seckill.repository.StockLogRepository;
import com.rong.seckill.util.validator.Validator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @Author chenrong
 * @Date 2019-08-28 20:27
 **/
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private SequenceRepository sequenceRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StockLogRepository stockLogRepository;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    private ExecutorService executorService;

    private RateLimiter orderCreateRateLimiter;

    @PostConstruct
    public void init(){
        executorService = Executors.newFixedThreadPool(20);
        orderCreateRateLimiter = RateLimiter.create(300);

    }

    @Override
    public void create(Integer itemId, Integer amount, Integer promoId, String promoToken, UserModel userModel) throws BusinessException {
        //活动太火爆，请稍后再试
        if(!orderCreateRateLimiter.tryAcquire()) {
            throw new BusinessException(EmBusinessError.RATELIMIT);
        }
        //校验秒杀令牌是否正确
        if(!Validator.isNull(promoId)) {
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_" + promoId + "_userid_" + userModel.getId() + "_itemid_" + itemId);
            if(Validator.isNull(inRedisPromoToken)) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀令牌校验失败");
            }
            if(!org.apache.commons.lang3.StringUtils.equals(promoToken, inRedisPromoToken)) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀令牌校验失败");
            }
        }
        //下游拥塞窗口为20的等待队列，用来队列化泄洪
        Future<Object> future = executorService.submit(() -> {
            //加入库存流水init状态
            String stockLogId = itemService.initStockLog(itemId, amount);
            //下单事务型消息机制
            if(!mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, promoId, amount, stockLogId)){
                throw new BusinessException(EmBusinessError.UNKNOWN_ERROR,"下单失败");
            }
            return null;
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }
    }

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws BusinessException {
        //1.校验下单状态,下单的商品是否存在，用户是否合法，购买数量是否正确 移到令牌创建
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(Validator.isNull(itemModel)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }
        if(amount <= 0 || amount > 99){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"数量信息不正确");
        }
        //2.落单减库存（redis）
        boolean result = itemService.decreaseStock(itemId, amount);
        if(!result){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if(Validator.isNull(promoId)) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        //生成交易流水号,订单号
        orderModel.setId(generateOrderNo());
        Order order = convertFromOrderModel(orderModel);
        orderRepository.save(order);

        //todo 加上商品的销量，也需要异步化
        itemService.increaseSales(itemId, amount);

        //设置库存流水状态为成功
        StockLog stockLogDO = stockLogRepository.getOne(stockLogId);
        if(Validator.isNull(stockLogDO)) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }
        stockLogDO.setStatus(StockLogStatus.SUCCESS.getCode());
        stockLogRepository.save(stockLogDO);

        // // 最近Transaction注解，成功commit后，被执行
        // TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
        //         @Override
        //         public void afterCommit(){
        //             //需要保证mq必定发送成功（之前数据库已操作成功）
        //             boolean mqResult = itemService.asyncDecreaseStock(itemId,amount);
        //         }
        // });
        return orderModel;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNo() {
        //订单号有16位
        StringBuilder stringBuilder = new StringBuilder();
        //前8位为时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowDate);

        //中间6位为自增序列
        //获取当前sequence
        int sequence = 0;
        Sequence sequenceDO =  sequenceRepository.findByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceRepository.save(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for(int i = 0; i < 6-sequenceStr.length();i++){
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);

        //最后2位为分库分表位,暂时写死
        stringBuilder.append("00");
        return stringBuilder.toString();
    }

    @Override
    public String generateToken(Integer itemId, Integer promoId, UserModel userModel) throws BusinessException {
        //获取秒杀访问令牌
        String promoToken = promoService.generateSecondKillToken(promoId, itemId, userModel.getId());
        if(Validator.isNull(promoToken)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"生成令牌失败");
        }
        return promoToken;
    }

    private Order convertFromOrderModel(OrderModel orderModel) {
        if(Validator.isNull(orderModel)) {
            return null;
        }
        Order order = new Order();
        BeanUtils.copyProperties(orderModel,order);
        order.setItemPrice(orderModel.getItemPrice().doubleValue());
        order.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return order;
    }
}
