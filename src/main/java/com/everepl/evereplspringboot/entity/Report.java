package com.everepl.evereplspringboot.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter; // 신고한 사용자

    @Embedded
    private Target target;

    @Enumerated(EnumType.STRING)
    private ReportReason reason; // 신고 사유

    private LocalDateTime reportedAt;

    public enum ReportReason {
        INAPPROPRIATE_PROFILE_PICTURE,
        INAPPROPRIATE_COMMENT,
        INAPPROPRIATE_URL
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public ReportReason getReason() {
        return reason;
    }

    public void setReason(ReportReason reason) {
        this.reason = reason;
    }

    public LocalDateTime getReportedAt() {
        if (reportedAt != null) {
            return reportedAt.withSecond(0).withNano(0);
        }
        return null;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }
}
