package com.rong.seckill.repository;

import com.rong.seckill.infrastructure.entity.Order;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends BaseRepository<Order, Integer> {
}