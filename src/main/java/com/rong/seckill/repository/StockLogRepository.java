package com.rong.seckill.repository;

import com.rong.seckill.entity.StockLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockLogRepository extends BaseRepository<StockLog, String> {

}