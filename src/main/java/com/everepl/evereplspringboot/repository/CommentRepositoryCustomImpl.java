package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.QComment;
import com.everepl.evereplspringboot.entity.Target;
import com.querydsl.core.types.dsl.BooleanExpression;
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
    public List<Comment> findCommentsWithRepliesByTarget_TypeAndTarget_TargetId(Target.TargetType type, Long targetId, Pageable pageable) {
        QComment qComment = QComment.comment;

        JPAQuery<Comment> query = new JPAQuery<>(entityManager);

        BooleanExpression typeCondition = qComment.rootTargetType.eq(type);

        List<Comment> comments = query.select(qComment)
                .from(qComment)
                .where(qComment.path.startsWith(String.valueOf(targetId))
                        .and(typeCondition)) // 수정된 type 조건
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qComment.path.asc())
                .orderBy(qComment.createdAt.asc())
                .fetch();

        return comments;
    }

}