package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.Target;
import com.everepl.evereplspringboot.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment>, CommentRepositoryCustom  {
    Page<Comment> findAllByTarget_TypeAndTarget_TargetId(Target.TargetType type, Long targetId, Pageable pageable);
    Page<Comment> findByUser(User user, Pageable pageable);

}
