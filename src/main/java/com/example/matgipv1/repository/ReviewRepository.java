package com.example.matgipv1.repository;

import com.example.matgipv1.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByRestaurantIdAndMemberId(Long restaurantId, Long memberId);
    List<Review> findByMemberId(Long memberId); // 회원별 리뷰 조회 추가
    List<Review> findByRestaurantId(Long restaurantId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.restaurantId = :restaurantId")
    void deleteByRestaurantId(@Param("restaurantId") Long restaurantId);
}
