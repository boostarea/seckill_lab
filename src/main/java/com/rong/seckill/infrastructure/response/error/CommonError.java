package com.rong.seckill.infrastructure.response.error;

/**
 * @Author chenrong
 * @Date 2019-08-28 15:27
 **/
public interface CommonError {
    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);
}
