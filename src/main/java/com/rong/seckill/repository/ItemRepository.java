package com.rong.seckill.repository;

import com.rong.seckill.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ItemRepository extends BaseRepository<Item, Integer> {

    @Query("select t from Item t order by sales desc ")
    List<Item> listItem();

    @Modifying
    @Query("update Item set sales = sales + :amount where id=:id")
    int increaseSales(Integer id, Integer amount);
}