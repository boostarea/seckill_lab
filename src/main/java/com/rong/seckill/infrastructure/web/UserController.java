package com.rong.seckill.infrastructure.web;

import com.rong.seckill.domain.service.UserService;
import com.rong.seckill.infrastructure.response.CommonReturnType;
import com.rong.seckill.infrastructure.response.error.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * @Author chenrong
 * @Date 2019-08-28 20:27
 **/
@RestController
@RequestMapping("user")
@CrossOrigin(allowCredentials="true", allowedHeaders = "*")
public class UserController  extends BaseController{

    @Autowired
    private UserService userService;

    //用户登陆接口
    @PostMapping(value = "login", consumes={CONTENT_TYPE_FORMED})
    public CommonReturnType login(@RequestParam(name="telphone")String telphone,
                                  @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        String uuidToken = userService.login(telphone, password);
        return CommonReturnType.create(uuidToken);
    }

}
