package com.rong.seckill.infrastructure.web;

import com.rong.seckill.domain.model.UserModel;
import com.rong.seckill.domain.service.OrderService;
import com.rong.seckill.infrastructure.response.error.BusinessException;
import com.rong.seckill.infrastructure.response.error.EmBusinessError;
import com.rong.seckill.infrastructure.response.CommonReturnType;
import com.rong.seckill.util.validator.Validator;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author chenrong
 * @Date 2019-08-28 15:27
 **/
@RestController
@AllArgsConstructor
@RequestMapping("order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseController {

    private final OrderService orderService;

    private final HttpServletRequest httpServletRequest;

    private final  RedisTemplate redisTemplate;

    @RequestMapping(value = "generateverifycode",method = {RequestMethod.GET,RequestMethod.POST})
    public void generateverifycode(HttpServletResponse response) throws BusinessException {
        verifyToken();
    }

    //生成秒杀令牌
    @PostMapping(value = "generateToken", consumes={CONTENT_TYPE_FORMED})
    public CommonReturnType generateToken(@RequestParam(name="itemId")Integer itemId,
                                          @RequestParam(name="promoId")Integer promoId) throws BusinessException {

        UserModel userModel = verifyToken();
        String promoToken =orderService.generateToken(itemId, promoId, userModel);
        return CommonReturnType.create(promoToken);
    }

    //下单
    @PostMapping(value = "create", consumes={CONTENT_TYPE_FORMED})
    public CommonReturnType create(@RequestParam(name="itemId")Integer itemId,
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

