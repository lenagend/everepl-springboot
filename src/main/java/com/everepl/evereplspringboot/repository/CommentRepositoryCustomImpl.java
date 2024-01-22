package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.QComment;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    // EntityManager 주입
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Comment> findCommentsWithRepliesByTargetTypeAndTargetId(Comment.targetType type, Long targetId, Pageable pageable) {
        QComment qComment = QComment.comment;

        JPAQuery<Comment> query = new JPAQuery<>(entityManager);

        List<Comment> comments = query.select(qComment)
                .from(qComment)
                .where(  qComment.path.startsWith(String.valueOf(targetId))
                        .and(qComment.type.eq(type))) // 경로 기반 필터링
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qComment.path.asc())
                .orderBy(qComment.createdAt.asc())// 경로 기준으로 정렬
                .fetch();

        return comments;
    }



}