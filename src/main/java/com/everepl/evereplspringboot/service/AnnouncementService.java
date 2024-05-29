package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.AnnouncementRequest;
import com.everepl.evereplspringboot.dto.AnnouncementResponse;
import com.everepl.evereplspringboot.dto.UserResponse;
import com.everepl.evereplspringboot.entity.Announcement;
import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.repository.AnnouncementRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private final UserService userService;

    public AnnouncementService(AnnouncementRepository announcementRepository, UserService userService) {
        this.announcementRepository = announcementRepository;
        this.userService = userService;
    }

    public List<AnnouncementResponse> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(this::toAnnouncementResponse)
                .collect(Collectors.toList());
    }

    public AnnouncementResponse createAnnouncement(AnnouncementRequest request) {
        User currentUser = userService.getAuthenticatedUser();
        Announcement announcement = new Announcement();
        announcement.setTitle(request.title());
        announcement.setContent(request.content());
        announcement.setUser(currentUser);
        Announcement savedAnnouncement = announcementRepository.save(announcement);
        return toAnnouncementResponse(savedAnnouncement);
    }

    public AnnouncementResponse updateAnnouncement(Long id, AnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        User currentUser = userService.getAuthenticatedUser();
        if (!announcement.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("권한이 없습니다.");
        }
        announcement.setTitle(request.title());
        announcement.setContent(request.content());
        Announcement updatedAnnouncement = announcementRepository.save(announcement);
        return toAnnouncementResponse(updatedAnnouncement);
    }

    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        User currentUser = userService.getAuthenticatedUser();
        if (!announcement.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("권한이 없습니다.");
        }
        announcementRepository.delete(announcement);
    }

    private AnnouncementResponse toAnnouncementResponse(Announcement announcement) {
        User user = announcement.getUser();
        UserResponse userResponse = userService.toDto(user);
        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getCreatedAt(),
                announcement.getUpdatedAt(),
                userResponse
        );
    }
}