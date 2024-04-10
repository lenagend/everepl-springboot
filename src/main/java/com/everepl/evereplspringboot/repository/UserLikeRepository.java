package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.entity.UserLike;
import com.everepl.evereplspringboot.entity.Target;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserLikeRepository extends JpaRepository<UserLike, Long> {

    // 특정 IP에서 특정 대상(타입과 ID 모두 고려)에 대해 특정 날짜에 '좋아요'를 했는지 확인하는 메소드
    boolean existsByUserAndTargetTargetIdAndTargetTypeAndLikedDate(User user, Long targetId, Target.TargetType type, LocalDate likedDate);

    @Query("SELECT ul.target.targetId FROM UserLike ul WHERE ul.user = :user AND ul.target.type = :type")
    List<Long> findTargetIdsByUserAndTargetType(@Param("user") User user, @Param("type") Target.TargetType type);

}