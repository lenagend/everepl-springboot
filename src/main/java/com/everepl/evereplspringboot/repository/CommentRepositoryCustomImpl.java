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
        // QueryDSL을 사용하기 위한 QComment 인스턴스 생성
        QComment qComment = QComment.comment;

        // QueryDSL JPAQuery를 사용하여 쿼리 생성
        JPAQuery<Comment> query = new JPAQuery<>(entityManager);

        // 쿼리를 생성하여 반환할 결과 리스트
        List<Comment> comments = query.select(qComment)
                .from(qComment)
                // 대댓글을 포함시키기 위해 left join 사용
                .leftJoin(qComment.replies, qComment)
                .fetchJoin()
                // targetType과 targetId에 따라 필터링
                .where(qComment.type.eq(type)
                        .and(qComment.targetId.eq(targetId)))
                // Pageable 객체를 사용하여 페이징 처리
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                // 결과 리스트 가져오기
                .fetch();

        return comments;
    }

    @Override
    public long countTotalCommentsIncludingRepliesByTargetTypeAndTargetId(Comment.targetType type, Long targetId) {
        QComment qComment = QComment.comment;
        QComment qReply = new QComment("reply");

        // 쿼리에서 대댓글을 고려하여 전체 개수를 계산
        JPAQuery<Long> query = new JPAQuery<>(entityManager);

        long count = query.select(qComment.count().add(qReply.count()))
                .from(qComment)
                .leftJoin(qComment.replies, qReply)
                .where(qComment.type.eq(type)
                        .and(qComment.targetId.eq(targetId)))
                .fetchOne();

        return count;
    }

}