package com.rong.seckill.repository;

import com.rong.seckill.entity.Promo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoRepository extends JpaRepository<Promo, Integer> {

    Promo findByItemId(Integer itemId);
}