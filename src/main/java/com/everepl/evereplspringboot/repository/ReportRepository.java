package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.Report;
import com.everepl.evereplspringboot.entity.Target;
import com.everepl.evereplspringboot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterAndTargetAndReason(User reporter, Target target, Report.ReportReason reason);
    long countByTargetAndReason(Target target, Report.ReportReason reason);
}