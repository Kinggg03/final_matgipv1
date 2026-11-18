package com.example.matgipv1.service;

import com.example.matgipv1.domain.Place;
import com.example.matgipv1.repository.PlaceRepository;
import dto.PageRequestDTO;
import dto.PageResponseDTO;
import dto.PlaceDTO;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class PlaceServiceImpl implements PlaceService {
    private final PlaceRepository placeRepository;
    private final ReviewService reviewService;
    private final BookmarkService bookmarkService;
    private final ModelMapper modelMapper;

    @Override
    public Long register(PlaceDTO placeDTO) {
        Place place = dtoToEntity(placeDTO);
        Long pno = placeRepository.save(place).getPno();
        return pno;
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceDTO readOne(Long pno) {
        Optional<Place> result = placeRepository.findById(pno);
        Place place = result.orElseThrow(() -> new IllegalArgumentException("해당 맛집이 존재하지 않습니다. pno=" + pno));
        return entityToDto(place);
    }

    @Override
    public void modify(PlaceDTO placeDTO) {
        Optional<Place> result = placeRepository.findById(placeDTO.getPno());
        Place place = result.orElseThrow(() -> new IllegalArgumentException("해당 맛집이 존재하지 않습니다. pno=" + placeDTO.getPno()));

        place.change(
                placeDTO.getGuName(),
                placeDTO.getRestaurantNameKo(),
                placeDTO.getRestaurantNameEn(),
                placeDTO.getCategory(),
                placeDTO.getSummaryKo(),
                placeDTO.getSummaryEn(),
                placeDTO.getImageUrl1(),
                placeDTO.getImageUrl2(),
                placeDTO.getAddress(),
                placeDTO.getPhone(),
                placeDTO.getOperatingHours(),
                placeDTO.getBreakTime(),
                placeDTO.getOffDays(),
                placeDTO.getMainMenu(),
                placeDTO.getPriceRange(),
                placeDTO.getParking(),
                placeDTO.getPageNum()
        );

        placeRepository.save(place);
    }

    @Override
    public void remove(Long pno) {
        if (pno == null) return;
        Place place = placeRepository.findById(pno)
                .orElseThrow(() -> new IllegalArgumentException("해당 맛집이 존재하지 않습니다. pno=" + pno));
        // Place의 cascade 설정으로 리뷰와 찜도 함께 삭제합니다.
        placeRepository.delete(place);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PlaceDTO> getList(PageRequestDTO pageRequestDTO, Long memberNo) {
        Pageable pageable = pageRequestDTO.getPageable("pno");

        // 대분류 카테고리 검색어 정제 (예: "카페(커피)" -> "카페")
        String category = pageRequestDTO.getCategory();
        String searchCategory = category;
        if (searchCategory != null && searchCategory.contains("(")) {
            searchCategory = searchCategory.substring(0, searchCategory.indexOf("(")).trim();
        }

        Page<Place> result = placeRepository.searchList(
                pageRequestDTO.getGuName(),
                searchCategory,
                pageRequestDTO.getKeyword(),
                pageable
        );

        List<PlaceDTO> dtoList = result.getContent().stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());

        // [수정] 목록 카드용 평균 평점과 북마크 정보를 서비스에서 조합합니다.
        dtoList.forEach(place -> {
            List<dto.ReviewDTO> reviews = reviewService.getReviewsByRestaurant(place.getPno());
            double avgRating = reviews.isEmpty() ? 0.0 : Math.round(
                    reviews.stream().mapToInt(dto.ReviewDTO::getRating).average().orElse(0.0) * 10.0
            ) / 10.0;
            place.setAvgRating(avgRating);
            place.setReviewCount(reviews.size());
            place.setBookmarkCount(bookmarkService.getBookmarkCount(place.getPno()));
            place.setIsBookmarked(memberNo != null && bookmarkService.isBookmarked(memberNo, place.getPno()));
        });

        return PageResponseDTO.<PlaceDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceDTO> getAllPlaces() {
        return placeRepository.findAll().stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getGuList() {
        List<String> list = placeRepository.findDistinctGuNames();
        if (list == null || list.isEmpty()) {
            return List.of("강서구", "금정구", "기장군", "남구", "동구", "동래구", "부산진구", "북구", "사상구", "사하구", "서구", "수영구", "연제구", "영도구", "중구", "해운대구");
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCategoryList() {
        List<String> rawCategories = placeRepository.findDistinctCategories();
        if (rawCategories == null || rawCategories.isEmpty()) {
            return List.of("한식", "일식", "중식", "양식", "카페(커피)", "디저트", "분식", "아시안", "치킨");
        }

        // DB에 존재하는 실제 소분류/상세 카테고리(예: 한식/갈비, 카페/디저트 등)에서 대분류를 자동 추출
        Set<String> extractedFromDb = new LinkedHashSet<>();
        for (String raw : rawCategories) {
            if (raw == null || raw.trim().isEmpty()) continue;
            String mainCat = raw.trim();
            if (mainCat.contains("/")) {
                mainCat = mainCat.split("/")[0].trim();
            }
            if (!mainCat.isEmpty()) {
                extractedFromDb.add(mainCat);
            }
        }

        Set<String> majorCategories = new LinkedHashSet<>();
        // 사용자 편의성을 위한 주요 카테고리 우선 정렬 순서
        List<String> priorityOrder = List.of("한식", "일식", "중식", "양식", "카페", "디저트", "분식", "아시안", "치킨");

        for (String p : priorityOrder) {
            if (extractedFromDb.contains(p)) {
                if ("카페".equals(p)) {
                    majorCategories.add("카페(커피)");
                } else {
                    majorCategories.add(p);
                }
                extractedFromDb.remove(p);
            }
        }

        // 그 외 DB에 추가로 존재하는 대분류 추가
        for (String rest : extractedFromDb) {
            majorCategories.add(rest);
        }

        return new ArrayList<>(majorCategories);
    }
}
