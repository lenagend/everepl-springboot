package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.Target;
import com.everepl.evereplspringboot.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment>  {
    // 주 댓글과 관련된 데이터를 리스트 형태로 조회
    List<Comment> findByTarget_TypeAndTarget_TargetId(Target.TargetType type, Long targetId);

    // 특정 댓글의 대댓글을 조회
    List<Comment> findByParentCommentId(Long parentCommentId);

    //top3 대댓글만을 조회
    List<Comment> findTop3ByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    // 사용자가 작성한 댓글 조회
    Page<Comment> findByUser(User user, Pageable pageable);

    List<Comment> findByUser(User user);

}
