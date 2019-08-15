package com.rong.seckill.repository;

import com.rong.seckill.entity.ItemStock;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public interface ItemStockRepository extends BaseRepository<ItemStock, Integer> {

    ItemStock findByItemId(Integer itemId);

    @Modifying
    @Query("update ItemStock  set stock = stock - :amount where itemId = :itemId and stock >= :amount")
    int decreaseStock(@Param("itemId") Integer itemId, @Param("amount") Integer amount);
}