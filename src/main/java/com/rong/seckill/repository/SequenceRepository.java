package com.rong.seckill.repository;

import com.rong.seckill.entity.Sequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SequenceRepository extends JpaRepository<Sequence, Integer> {

    Sequence findByName(String name);
}