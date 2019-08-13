package com.rong.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author chenrong
 * @Date 2019-08-11 15:27
 **/
@SpringBootApplication(scanBasePackages = {"com.rong.seckill"})
@MapperScan("com.rong.seckill.repository")
public class App {

    public static void main( String[] args ) {
        SpringApplication.run(App.class,args);
    }
}
