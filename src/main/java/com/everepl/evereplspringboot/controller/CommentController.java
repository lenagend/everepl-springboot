package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.dto.CommentRequest;
import com.everepl.evereplspringboot.dto.CommentResponse;
import com.everepl.evereplspringboot.dto.UrlInfoResponse;
import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<?> addComment(HttpServletRequest request, @Valid @RequestBody CommentRequest commentRequest) {
        try {
            String userIp = request.getRemoteAddr();
            CommentResponse savedComment = commentService.addComment(commentRequest, userIp);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database access error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getComments(@ModelAttribute CommentRequest commentRequest, Pageable pageable) {
        try {
            Page<CommentResponse> comments = commentService.getComments(commentRequest, pageable);
            return ResponseEntity.ok(comments);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("error: " + e.getMessage());
        }
    }
}
