package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.UrlInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UrlInfoRepository extends JpaRepository<UrlInfo, Long>, JpaSpecificationExecutor<UrlInfo> {
    Optional<UrlInfo> findByUrl(String url);
}
