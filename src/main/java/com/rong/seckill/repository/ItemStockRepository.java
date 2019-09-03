package com.rong.seckill.repository;

import com.rong.seckill.repository.entity.ItemStock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface ItemStockRepository extends BaseRepository<ItemStock, Integer> {

    ItemStock findByItemId(Integer itemId);

    @Modifying
    @Transactional
    @Query("update ItemStock  set stock = stock - :amount where itemId = :itemId and stock >= :amount")
    int decreaseStock(@Param("itemId") Integer itemId, @Param("amount") Integer amount);
}