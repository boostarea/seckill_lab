package com.rong.seckill.infrastructure.response.error;

/**
 * @Author chenrong
 * @Date 2019-08-28 20:27
 **/
public interface CommonError {

    int getErrCode();

    String getErrMsg();

    CommonError setErrMsg(String errMsg);
}
