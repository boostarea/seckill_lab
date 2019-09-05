package com.rong.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/**
 * @Author chenrong
 * @Date 2019-08-28 20:27
 **/
@SpringBootApplication(scanBasePackages = {"com.rong.seckill"})
@EnableJpaRepositories(basePackages = "com.rong.seckill.repository")
public class App {

    public static void main( String[] args ) {
        SpringApplication.run(App.class,args);
    }
}
