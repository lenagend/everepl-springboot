package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.Comment;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findCommentsWithRepliesByTargetTypeAndTargetId(Comment.targetType type, Long targetId, Pageable pageable);
}
