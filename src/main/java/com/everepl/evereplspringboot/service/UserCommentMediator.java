package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCommentMediator {
    private UserService userService;

    private CommentService commentService;

    public UserCommentMediator(UserService userService, CommentService commentService) {
        this.userService = userService;
        this.commentService = commentService;
    }

    @Transactional
    public void deleteUserAndComments() {
        User currentUser = userService.getAuthenticatedUser();
        List<Comment> comments = commentService.getCommentsByUser(currentUser);
        commentService.deleteAllComments(comments);
        userService.deleteUser(currentUser.getId());
    }
}
