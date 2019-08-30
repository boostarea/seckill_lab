package com.rong.seckill.domain.service.impl;

import com.rong.seckill.domain.model.UserModel;
import com.rong.seckill.domain.service.UserService;
import com.rong.seckill.infrastructure.entity.User;
import com.rong.seckill.infrastructure.entity.UserPassword;
import com.rong.seckill.infrastructure.response.error.BusinessException;
import com.rong.seckill.infrastructure.response.error.EmBusinessError;
import com.rong.seckill.repository.UserPasswordRepository;
import com.rong.seckill.repository.UserRepository;
import com.rong.seckill.util.validator.ValidationResult;
import com.rong.seckill.util.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if(userModel == null){
            userModel = this.getUserById(id);
            redisTemplate.opsForValue().set("user_validate_"+id,userModel);
            redisTemplate.expire("user_validate_"+id,10, TimeUnit.MINUTES);
        }
        return userModel;
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        ValidationResult result =  validator.validate(userModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }



        //实现model->dataobject方法
        User user = convertFromModel(userModel);
        try{
            userRepository.save(user);
        }catch(DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已重复注册");
        }



        userModel.setId(user.getId());

        UserPassword userPassword = convertPasswordFromModel(userModel);
        userPasswordRepository.save(userPassword);

        return;
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


    private UserPassword convertPasswordFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserPassword userPassword = new UserPassword();
        userPassword.setEncrptPassword(userModel.getEncrptPassword());
        userPassword.setUserId(userModel.getId());
        return userPassword;
    }
    private User convertFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        User userDO = new User();
        BeanUtils.copyProperties(userModel,userDO);

        return userDO;
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
}
