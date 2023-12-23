package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.UrlInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlInfoRepository extends JpaRepository<UrlInfo, String> {
    Optional<UrlInfo> findByUrl(String url);
}
