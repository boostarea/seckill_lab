package com.rong.seckill.repository;

import com.rong.seckill.entity.Sequence;
import org.springframework.stereotype.Repository;

@Repository
public interface SequenceRepository extends BaseRepository<Sequence, Integer> {

    Sequence findByName(String name);
}