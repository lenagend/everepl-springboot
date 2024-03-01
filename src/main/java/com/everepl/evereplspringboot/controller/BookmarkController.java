package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.dto.BookmarkRequest;
import com.everepl.evereplspringboot.dto.BookmarkResponse;
import com.everepl.evereplspringboot.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @Autowired
    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/process")
    public ResponseEntity<Page<?>> processBookmarks(@RequestBody BookmarkRequest bookmarkRequest, Pageable pageable) {
        try {
            Page<?> responses = bookmarkService.processBookmarks(bookmarkRequest, pageable);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
