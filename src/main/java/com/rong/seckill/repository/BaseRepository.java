package com.rong.seckill.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

/**
 * @Description TODO
 * @Author chenrong
 * @Date 2019-08-15 16:06
 **/
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    default <S extends T> S save(S entity) {
        //todo 分布式ID

        return save(entity);
    }
}
