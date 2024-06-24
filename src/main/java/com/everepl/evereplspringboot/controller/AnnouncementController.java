package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.dto.AnnouncementRequest;
import com.everepl.evereplspringboot.dto.AnnouncementResponse;
import com.everepl.evereplspringboot.service.AnnouncementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getAllAnnouncements() {
        List<AnnouncementResponse> announcementResponseList = announcementService.getAllAnnouncements();
        return ResponseEntity.ok(announcementResponseList);
    }

}