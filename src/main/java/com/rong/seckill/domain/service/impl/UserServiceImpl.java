package com.rong.seckill.domain.service.impl;

import com.rong.seckill.domain.model.UserModel;
import com.rong.seckill.domain.service.UserService;
import com.rong.seckill.infrastructure.response.error.BusinessException;
import com.rong.seckill.infrastructure.response.error.EmBusinessError;
import com.rong.seckill.repository.UserPasswordRepository;
import com.rong.seckill.repository.UserRepository;
import com.rong.seckill.repository.entity.User;
import com.rong.seckill.repository.entity.UserPassword;
import com.rong.seckill.util.validator.Validator;
import com.rong.seckill.util.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author chenrong
 * @Date 2019-08-28 15:27
 **/
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPasswordRepository userPasswordRepository;

    @Autowired
    private ValidatorImpl validator;


    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public UserModel getUserById(Integer id) {
        //调用userdomapper获取到对应的用户dataobject
        User user = userRepository.getOne(id);
        if(user == null){
            return null;
        }
        //通过用户id获取对应的用户加密密码信息
        UserPassword userPassword = userPasswordRepository.findByUserId(user.getId());

        return convertFromDataObject(user,userPassword);
    }

    @Override
    public UserModel getUserByIdInCache(Integer id) {
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get("user_validate_"+id);
        if(Validator.isNull(userModel)) {
            userModel = this.getUserById(id);
            redisTemplate.opsForValue().set("user_validate_"+id,userModel);
            redisTemplate.expire("user_validate_"+id,10, TimeUnit.MINUTES);
        }
        return userModel;
    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        //通过用户的手机获取用户信息
        User user = userRepository.findByTelphone(telphone);
        if(user == null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPassword userPassword = userPasswordRepository.findByUserId(user.getId());
        UserModel userModel = convertFromDataObject(user,userPassword);

        //比对用户信息内加密的密码是否和传输进来的密码相匹配
        if(!StringUtils.equals(encrptPassword,userModel.getEncrptPassword())){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }

    @Override
    public String login(String telphone, String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if(org.apache.commons.lang3.StringUtils.isEmpty(telphone)||
                org.springframework.util.StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //用户登陆服务,用来校验用户登陆是否合法
        UserModel userModel = validateLogin(telphone, EncodeByMd5(password));
        //将登陆凭证加入到用户登陆成功的session内

        //修改成若用户登录验证成功后将对应的登录信息和登录凭证一起存入redis中

        //生成登录凭证token，UUID
        String uuidToken = UUID.randomUUID().toString();
        uuidToken = uuidToken.replace("-","");

        uuidToken = "2a605b86b7d9423e80c79c47df866fb1";
        //建议token和用户登陆态之间的联系
        redisTemplate.opsForValue().set(uuidToken,userModel);
        redisTemplate.expire(uuidToken,1, TimeUnit.HOURS);

        return uuidToken;
    }

    private UserModel convertFromDataObject(User userDO, UserPassword userPasswordDO){
        if(userDO == null){
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);

        if(userPasswordDO != null){
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }

        return userModel;
    }

    private String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //加密字符串
        String newstr = base64en.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }
}
