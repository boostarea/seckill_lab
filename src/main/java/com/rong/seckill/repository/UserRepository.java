package com.rong.seckill.repository;

import com.rong.seckill.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseRepository<User, Integer> {

    User findByTelphone(String telphone);
}