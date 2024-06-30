package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.TrendingUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrendingUrlRepository extends JpaRepository<TrendingUrl, Long> {
    List<TrendingUrl> findByDate(LocalDate date);
}