package com.talktrip.talktrip.domain.like.repository;

import com.talktrip.talktrip.domain.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {

    /** Redis 적재용: 회원별 좋아요 + 상품 fetch join (lazy 오류 방지) */
    @Query("select l from Like l join fetch l.product where l.member.id = :memberId")
    List<Like> findAllByMember_IdWithProduct(@Param("memberId") Long memberId);
}
