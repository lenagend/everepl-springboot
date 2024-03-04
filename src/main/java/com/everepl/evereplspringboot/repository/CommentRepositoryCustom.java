package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.Target;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findCommentsWithRepliesByTarget_TypeAndTarget_TargetId(Target.TargetType type, Long targetId, Pageable pageable);
}
