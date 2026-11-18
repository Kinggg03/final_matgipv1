package com.example.matgipv1.repository;

import com.example.matgipv1.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByPlace_PnoAndMember_Mno(Long restaurantId, Long memberId);

    // 회원별 작성 리뷰 최신순 조회 (마이페이지용)
    List<Review> findByMember_MnoOrderByRegDateDesc(Long memberId);

    // 식당별 전체 리뷰 최신순 조회
    List<Review> findByPlace_PnoOrderByRegDateDesc(Long restaurantId);

    // [수정] 사용자 상세 화면에는 관리자가 숨김 처리한 리뷰를 제외합니다.
    @Query("SELECT r FROM Review r WHERE r.place.pno = :restaurantId " +
            "AND (r.hidden = false OR r.hidden IS NULL) ORDER BY r.regDate DESC")
    List<Review> findVisibleByRestaurantIdOrderByRegDateDesc(@Param("restaurantId") Long restaurantId);

    // [수정] 최신 리뷰 3건도 숨김 처리된 리뷰를 제외합니다.
    @Query("SELECT r FROM Review r WHERE r.place.pno = :restaurantId " +
            "AND (r.hidden = false OR r.hidden IS NULL) ORDER BY r.regDate DESC")
    List<Review> findVisibleReviewsByRestaurantIdOrderByRegDateDesc(@Param("restaurantId") Long restaurantId);

    // 시스템 전체 리뷰 최신순 조회 (관리자용)
    List<Review> findAllByOrderByRegDateDesc();

}
