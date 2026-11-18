package com.example.matgipv1.service;

import com.example.matgipv1.domain.review.SoloLevel;
import dto.ReviewDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {

    List<ReviewDTO> getReviewsByRestaurant(Long restaurantId);

    List<ReviewDTO> getTop3ReviewsByRestaurant(Long restaurantId);

    List<ReviewDTO> getReviewsByMember(Long memberId);

    List<ReviewDTO> getAllReviewsForAdmin();

    boolean changeHiddenStatus(Long reviewId, boolean hidden);

    Long addReview(Long restaurantId, Long memberId, Integer rating, SoloLevel soloLevel,
                   String content, MultipartFile file);

    boolean modifyReview(Long reviewId, Long memberId, boolean isAdmin,
                         Integer rating, SoloLevel soloLevel, String content,
                         MultipartFile file, boolean deleteImage);

    boolean deleteReview(Long reviewId, Long memberId, boolean isAdmin);
}

