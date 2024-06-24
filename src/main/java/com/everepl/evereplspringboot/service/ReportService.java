package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.ReportRequest;
import com.everepl.evereplspringboot.entity.*;
import com.everepl.evereplspringboot.exceptions.UserActionRestrictionException;
import com.everepl.evereplspringboot.repository.BlockedDomainRepository;
import com.everepl.evereplspringboot.repository.CommentRepository;
import com.everepl.evereplspringboot.repository.ReportRepository;
import com.everepl.evereplspringboot.repository.UrlInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final UrlInfoService urlInfoService;

    @Value("${report.profile-picture-violation-threshold}")
    private int profilePictureViolationThreshold;

    @Value("${report.comment-violation-threshold}")
    private int commentViolationThreshold;

    @Value("${report.url-violation-threshold}")
    private int urlViolationThreshold;

    @Value("${report.profile-picture-ban-duration}")
    private int profilePictureBanDurationDays;

    @Value("${report.comment-ban-duration}")
    private int commentBanDurationDays;

    public ReportService(ReportRepository reportRepository, UserService userService, CommentRepository commentRepository, UrlInfoService urlInfoService) {
        this.reportRepository = reportRepository;
        this.userService = userService;
        this.commentRepository = commentRepository;
        this.urlInfoService = urlInfoService;
    }

    public void handleReport(ReportRequest reportRequest) {
        User reporter = userService.getAuthenticatedUser();

        // 중복 신고 방지 로직 추가
        if (reportRepository.existsByReporterAndTargetAndReason(reporter, reportRequest.target(), reportRequest.reason())) {
            throw new UserActionRestrictionException("이미 같은 이유로 신고하셨습니다.");
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setTarget(reportRequest.target());
        report.setReason(reportRequest.reason());
        report.setReportedAt(LocalDateTime.now());

        reportRepository.save(report);

        switch (reportRequest.target().getType()) {
            case URLINFO:
                handleUrlReport(reportRequest.target().getTargetId());
                break;
            case COMMENT:
                handleCommentReport(reportRequest.target().getTargetId());
                break;
            case USER:
                handleUserReport(reportRequest.target().getTargetId(), reportRequest.reason());
                break;
            default:
                throw new IllegalArgumentException("잘못된 대상 유형입니다: " + reportRequest.target().getType());
        }
    }

    private void handleUserReport(Long userId, Report.ReportReason reason) {
        User reportedUser = userService.findUserById(userId);

        if (reason == Report.ReportReason.INAPPROPRIATE_PROFILE_PICTURE) {
            long reportCount = reportRepository.countByTargetAndReason(new Target(userId, Target.TargetType.USER), reason);
            if (reportCount >= profilePictureViolationThreshold) {
                reportedUser.setImageUrl(null);
                reportedUser.setProfilePictureBanUntil(LocalDateTime.now().plusDays(profilePictureBanDurationDays));
                userService.saveUser(reportedUser);
            }
        }
    }

    private void handleCommentReport(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다. ID: " + commentId));
        comment.updateReportCount(1);

        long reportCount = reportRepository.countByTargetAndReason(new Target(commentId, Target.TargetType.COMMENT), Report.ReportReason.INAPPROPRIATE_COMMENT);
        if (reportCount >= commentViolationThreshold) {
            User commentAuthor = comment.getUser();
            commentAuthor.setCommentBanUntil(LocalDateTime.now().plusDays(commentBanDurationDays));
            userService.saveUser(commentAuthor);
        }
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    @Transactional
    private void handleUrlReport(Long urlInfoId) {
        UrlInfo urlInfo = urlInfoService.getUrlInfoById(urlInfoId);

        urlInfo.incrementReportCount();
        int reportCount = urlInfo.getReportCount();

        if (reportCount >= urlViolationThreshold) {
            // 신고 수가 임계값에 도달하면 URL을 차단
            urlInfoService.blockUrl(urlInfo.getUrl());
        } else {
            // 신고 수가 임계값에 도달하지 않았을 경우 설명 업데이트
            urlInfo.setDescription(String.format("주의. 신고가 %d번 이상 된 URL입니다.", reportCount));
            urlInfoService.saveUrlInfo(urlInfo);
        }
    }

}
