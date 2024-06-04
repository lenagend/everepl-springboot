package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.BlockedDomain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockedDomainRepository extends JpaRepository<BlockedDomain, Long> {
    boolean existsByDomain(String domain);
}