package com.rong.seckill.repository;

import com.rong.seckill.entity.UserPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPasswordRepository extends JpaRepository<UserPassword, Integer> {

    UserPassword findByUserId(Integer userId);
}