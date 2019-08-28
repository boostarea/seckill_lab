package com.rong.seckill.repository;

import com.rong.seckill.entity.Promo;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoRepository extends BaseRepository<Promo, Integer> {

    Promo findByItemId(Integer itemId);
}