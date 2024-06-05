package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.UrlInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UrlInfoRepository extends JpaRepository<UrlInfo, Long>, JpaSpecificationExecutor<UrlInfo> {
    Optional<UrlInfo> findByUrl(String url);
    List<UrlInfo> findByDomain(String domain);
    Page<UrlInfo> findByTitleContainingIgnoreCase(String title, Pageable pageable);

}
