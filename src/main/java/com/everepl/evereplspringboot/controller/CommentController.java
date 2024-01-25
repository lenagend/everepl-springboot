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
    public ResponseEntity<?> addComment(HttpServletRequest request, @RequestBody CommentRequest commentRequest) {
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

    @PostMapping("/updateOrDeleteComment")
    public ResponseEntity<?> updateOrDeleteComment(
            @RequestBody CommentRequest commentRequest) {

        try {
            CommentResponse updatedComment = commentService.updateOrDeleteComment(commentRequest);
            return ResponseEntity.ok(updatedComment);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("댓글을 찾을 수 없습니다: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("잘못된 요청: " + e.getMessage());
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("데이터베이스 접근 오류: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("요청 처리 오류: " + e.getMessage());
        }
    }

}
