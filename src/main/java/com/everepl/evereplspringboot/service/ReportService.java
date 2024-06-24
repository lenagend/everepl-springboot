package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.ReportRequest;
import com.everepl.evereplspringboot.entity.*;
import com.everepl.evereplspringboot.exceptions.UserActionRestrictionException;
import com.everepl.evereplspringboot.repository.BlockedDomainRepository;
import com.everepl.evereplspringboot.repository.CommentRepository;
import com.everepl.evereplspringboot.repository.ReportRepository;
import com.everepl.evereplspringboot.repository.UrlInfoRepository;
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
    private final UrlInfoRepository urlInfoRepository;
    private final BlockedDomainRepository blockedDomainRepository;

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

    public ReportService(ReportRepository reportRepository, UserService userService, CommentRepository commentRepository, UrlInfoRepository urlInfoRepository, BlockedDomainRepository blockedDomainRepository) {
        this.reportRepository = reportRepository;
        this.userService = userService;
        this.commentRepository = commentRepository;
        this.urlInfoRepository = urlInfoRepository;
        this.blockedDomainRepository = blockedDomainRepository;
    }

    public void handleReport(ReportRequest reportRequest) {
        User reporter = userService.getAuthenticatedUser();
        boolean isAdmin = reporter.getRoles().contains(User.Role.ROLE_ADMIN);

        // 중복 신고 방지 로직 추가
        if (!isAdmin && reportRepository.existsByReporterAndTargetAndReason(reporter, reportRequest.target(), reportRequest.reason())) {
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
                handleUrlReport(reportRequest.target().getTargetId(), isAdmin);
                break;
            case COMMENT:
                handleCommentReport(reportRequest.target().getTargetId(), isAdmin);
                break;
            case USER:
                handleUserReport(reportRequest.target().getTargetId(), reportRequest.reason(), isAdmin);
                break;
            default:
                throw new IllegalArgumentException("잘못된 대상 유형입니다: " + reportRequest.target().getType());
        }
    }

    private void handleUserReport(Long userId, Report.ReportReason reason, boolean isAdmin) {
        User reportedUser = userService.findUserById(userId);

        if (reason == Report.ReportReason.INAPPROPRIATE_PROFILE_PICTURE) {
            long reportCount = reportRepository.countByTargetAndReason(new Target(userId, Target.TargetType.USER), reason);
            if (isAdmin || reportCount >= profilePictureViolationThreshold) {
                reportedUser.setImageUrl(null);
                reportedUser.setProfilePictureBanUntil(LocalDateTime.now().plusDays(profilePictureBanDurationDays));
                userService.saveUser(reportedUser);
            }
        }
    }

    private void handleCommentReport(Long commentId, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다. ID: " + commentId));
        comment.updateReportCount(1);

        long reportCount = reportRepository.countByTargetAndReason(new Target(commentId, Target.TargetType.COMMENT), Report.ReportReason.INAPPROPRIATE_COMMENT);
        if (isAdmin || reportCount >= commentViolationThreshold) {
            User commentAuthor = comment.getUser();
            commentAuthor.setCommentBanUntil(LocalDateTime.now().plusDays(commentBanDurationDays));
            userService.saveUser(commentAuthor);
        }
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    private void handleUrlReport(Long urlInfoId, boolean isAdmin) {
        UrlInfo urlInfo = urlInfoRepository.findById(urlInfoId)
                .orElseThrow(() -> new NoSuchElementException("URL 정보를 찾을 수 없습니다. ID: " + urlInfoId));

        urlInfo.incrementReportCount();
        int reportCount = urlInfo.getReportCount();
        if (reportCount >= urlViolationThreshold) {
            urlInfo.setDescription(String.format("주의. 신고가 %d번 이상 된 URL입니다.", reportCount));
            urlInfoRepository.save(urlInfo);
        }
        if (isAdmin) {
            String domain = urlInfo.getDomain();

            // 신고된 도메인을 블록 목록에 추가
            BlockedDomain blockedDomain = new BlockedDomain();
            blockedDomain.setDomain(domain);
            blockedDomainRepository.save(blockedDomain);

            // 해당 도메인의 기존 UrlInfo 삭제
            List<UrlInfo> urlInfosToDelete = urlInfoRepository.findByDomain(domain);
            urlInfoRepository.deleteAll(urlInfosToDelete);
        }
    }

}
