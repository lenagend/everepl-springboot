package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByOrderByUpdatedAtDesc();
}