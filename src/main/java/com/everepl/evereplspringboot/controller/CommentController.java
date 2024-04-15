package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.dto.CommentRequest;
import com.everepl.evereplspringboot.dto.CommentResponse;
import com.everepl.evereplspringboot.dto.validation.CreateGroup;
import com.everepl.evereplspringboot.dto.validation.ReadGroup;
import com.everepl.evereplspringboot.dto.validation.UpdateGroup;
import com.everepl.evereplspringboot.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<?> addComment(@Validated(CreateGroup.class) @RequestBody CommentRequest commentRequest) {
        // CommentService에 CommentRequest와 함께 userId도 전달
        CommentResponse savedComment = commentService.addComment(commentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
    }


    @GetMapping
    public ResponseEntity<?> getComments(@Validated(ReadGroup.class) @ModelAttribute CommentRequest commentRequest, Pageable pageable) {
            Page<CommentResponse> comments = commentService.getComments(commentRequest, pageable);
            return ResponseEntity.ok(comments);
    }


    @PatchMapping
    public ResponseEntity<?> updateComment(
            @Validated(UpdateGroup.class) @RequestBody CommentRequest commentRequest) {
            CommentResponse updatedComment = commentService.updateComment(commentRequest);
            return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);  // 실제로는 isDeleted를 true로 설정
        return ResponseEntity.ok().build();
    }

}
