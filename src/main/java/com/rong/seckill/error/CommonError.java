package com.rong.seckill.error;

/**
 * @Author chenrong
 * @Date 2019-08-11 15:27
 **/
public interface CommonError {
    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);
}
