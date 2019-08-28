package com.rong.seckill.repository;

import com.rong.seckill.entity.UserPassword;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPasswordRepository extends BaseRepository<UserPassword, Integer> {

    UserPassword findByUserId(Integer userId);
}