package com.rong.seckill.domain.service;

import com.rong.seckill.domain.model.UserModel;
import com.rong.seckill.error.BusinessException;

/**
 * @Author chenrong
 * @Date 2019-08-11 15:27
 **/
public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserById(Integer id);

    //通过缓存获取用户对象
    UserModel getUserByIdInCache(Integer id);

    void register(UserModel userModel) throws BusinessException;

    /*
    telphone:用户注册手机
    password:用户加密后的密码
     */
    UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException;
}
