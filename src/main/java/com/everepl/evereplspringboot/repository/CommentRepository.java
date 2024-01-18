package com.everepl.evereplspringboot.repository;

import com.everepl.evereplspringboot.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom  {
}
