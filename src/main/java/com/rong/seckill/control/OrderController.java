package com.rong.seckill.control;

import com.google.common.util.concurrent.RateLimiter;
import com.rong.seckill.domain.model.UserModel;
import com.rong.seckill.domain.service.ItemService;
import com.rong.seckill.domain.service.OrderService;
import com.rong.seckill.domain.service.PromoService;
import com.rong.seckill.error.BusinessException;
import com.rong.seckill.error.EmBusinessError;
import com.rong.seckill.mq.MqProducer;
import com.rong.seckill.response.CommonReturnType;
import com.rong.seckill.util.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @Author chenrong
 * @Date 2019-08-11 15:27
 **/
@RestController
@RequestMapping("order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "generateverifycode",method = {RequestMethod.GET,RequestMethod.POST})
    public void generateverifycode(HttpServletResponse response) throws BusinessException {
        verifyToken();
    }

    //生成秒杀令牌
    @PostMapping(value = "generatetoken", consumes={CONTENT_TYPE_FORMED})
    public CommonReturnType generatetoken(@RequestParam(name="itemId")Integer itemId,
                                          @RequestParam(name="promoId")Integer promoId) throws BusinessException {

        UserModel userModel = verifyToken();
        String promoToken =orderService.generateToken(itemId, promoId, userModel);
        //返回对应的结果
        return CommonReturnType.create(promoToken);
    }

    //下单
    @PostMapping(value = "createorder", consumes={CONTENT_TYPE_FORMED})
    public CommonReturnType createOrder(@RequestParam(name="itemId")Integer itemId,
                                        @RequestParam(name="amount")Integer amount,
                                        @RequestParam(name="promoId",required = false)Integer promoId,
                                        @RequestParam(name="promoToken",required = false)String promoToken) throws BusinessException {

        orderService.create(itemId, amount, promoId, promoToken, verifyToken());
        return CommonReturnType.create(null);
    }


    private UserModel verifyToken() throws BusinessException {
        //根据token获取用户信息
        String token = "2a605b86b7d9423e80c79c47df866fb1";
        // String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }
        //获取用户的登陆信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(Validator.isNull(userModel)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }
        return userModel;
    }
}
