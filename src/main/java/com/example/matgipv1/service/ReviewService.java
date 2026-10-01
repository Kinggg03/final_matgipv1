package com.example.matgipv1.service;

import com.example.matgipv1.repository.PlaceRepository;
import com.example.matgipv1.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final PlaceRepository placeRepository;
    //private final FileStore fileStore;

//    @Transactional
//    public Long createReview(ReviewRequest request, Member member) throws IOException {
//        // 1. 맛집 조회 (ErrorCode는 프로젝트에 정의된 에러 코드 상수로 지정)
//        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
//                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
//
//        // 2. 파일 저장 및 썸네일 생성
//        FileStore.UploadFile uploadFile = fileStore.storeFile(request.getImageFile());
//
//        // 3. 리뷰 엔티티 생성 (필요한 빌더 필드에 맞추어 구성)
//        Review review = Review.builder()
//                .restaurantId(restaurant.getId())
//                .memberId(member.getId())
//                .rating(request.getRating())
//                .content(request.getContent())
//                .originalImageName(uploadFile != null ? uploadFile.originalFileName() : null)
//                .storeImageName(uploadFile != null ? uploadFile.storeFileName() : null)
//                .thumbnailName(uploadFile != null ? uploadFile.thumbnailFileName() : null)
//                .build();
//
//        // 4. Restaurant 엔티티에 이미 구현된 applyNewReview 메서드를 호출하여 평점 및 혼밥 지수 갱신
//        restaurant.applyNewReview(request.getRating(), request.getSoloLevel().ordinal() + 1);
//
//        reviewRepository.save(review);
//        return review.getId();
//    }
}
