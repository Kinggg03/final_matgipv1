package com.example.matgipv1.service;

import com.example.matgipv1.domain.Member;
import com.example.matgipv1.domain.review.Review;
import com.example.matgipv1.domain.review.SoloLevel;
import com.example.matgipv1.repository.MemberRepository;
import com.example.matgipv1.repository.ReviewRepository;
import dto.ReviewDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    // 해당 맛집의 리뷰 목록 조회 (작성자 닉네임 포함)
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByRestaurant(Long restaurantId) {
        List<Review> reviews = reviewRepository.findByRestaurantId(restaurantId);

        // 작성자 닉네임 매핑을 위해 회원 정보 일괄 조회
        List<Long> memberIds = reviews.stream()
                .map(Review::getMemberId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> memberNickMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getMno, Member::getNickname, (n1, n2) -> n1));

        return reviews.stream().map(r -> {
            String nickname = memberNickMap.getOrDefault(r.getMemberId(), "익명회원");
            return ReviewDTO.builder()
                    .id(r.getId())
                    .restaurantId(r.getRestaurantId())
                    .memberId(r.getMemberId())
                    .writerNickname(nickname)
                    .rating(r.getRating() != null ? r.getRating() : 5)
                    .soloLevel(r.getSoloLevel())
                    .content(r.getContent())
                    .regDate(r.getRegDate())
                    .build();
        }).collect(Collectors.toList());
    }

    // 리뷰 신규 등록
    public Long addReview(Long restaurantId, Long memberId, Integer rating, SoloLevel soloLevel, String content) {
        if (rating == null || rating < 1) rating = 5;
        if (rating > 5) rating = 5;

        Review review = Review.builder()
                .restaurantId(restaurantId)
                .memberId(memberId)
                .rating(rating)
                .soloLevel(soloLevel != null ? soloLevel : SoloLevel.LV1)
                .content(content != null ? content.trim() : "")
                .hidden(false)
                .build();

        Review saved = reviewRepository.save(review);
        log.info("리뷰 등록 완료 - reviewId: {}, restaurantId: {}, memberId: {}", saved.getId(), restaurantId, memberId);
        return saved.getId();
    }

    // 리뷰 삭제 (작성자 본인 또는 관리자만 가능)
    public boolean deleteReview(Long reviewId, Long memberId, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return false;
        }

        if (isAdmin || (memberId != null && review.getMemberId().equals(memberId))) {
            reviewRepository.deleteById(reviewId);
            log.info("리뷰 삭제 완료 - reviewId: {}", reviewId);
            return true;
        }

        log.warn("리뷰 삭제 권한 없음 - reviewId: {}, memberId: {}", reviewId, memberId);
        return false;
    }
}
