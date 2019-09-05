package com.rong.seckill.domain.service;

import com.rong.seckill.domain.model.UserModel;
import com.rong.seckill.infrastructure.response.error.BusinessException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * @Author chenrong
 * @Date 2019-08-28 20:27
 **/
public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserById(Integer id);

    //通过缓存获取用户对象
    UserModel getUserByIdInCache(Integer id);

    UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException;

    String login(String telphone, String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException;
}
