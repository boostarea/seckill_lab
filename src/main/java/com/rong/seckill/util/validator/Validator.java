package com.rong.seckill.util.validator;

import java.util.Optional;

/**
 * @Description TODO
 * @Author chenrong
 * @Date 2019-08-28 11:36
 **/
public class Validator {

    public static <T> boolean isNull(T t) {
        return Optional.ofNullable(t).isPresent();
    }
}
