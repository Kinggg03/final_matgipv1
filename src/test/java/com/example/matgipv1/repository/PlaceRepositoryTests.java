package com.example.matgipv1.repository;

import com.example.matgipv1.service.GachaService;
import com.example.matgipv1.service.PlaceService;
import dto.GachaResponseDTO;
import dto.PageRequestDTO;
import dto.PageResponseDTO;
import dto.PlaceDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PlaceRepositoryTests {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private PlaceService placeService;

    @Autowired
    private GachaService gachaService;

    @Test
    public void testPrintCategories() {
        List<String> list = placeService.getCategoryList();
        System.out.println("====== MAJOR CATEGORIES START ======");
        for (String cat : list) {
            System.out.println("MAJOR CAT: [" + cat + "]");
        }
        System.out.println("====== MAJOR CATEGORIES END ======");
        assertNotNull(list);
        assertTrue(list.contains("한식"));
        assertTrue(list.contains("카페(커피)"));
        assertTrue(list.contains("디저트"));
    }

    @Test
    public void testSearchWithMajorCategory() {
        // 1. 카페(커피) 검색 테스트
        PageRequestDTO requestDTO = PageRequestDTO.builder()
                .category("카페(커피)")
                .size(5)
                .build();
        PageResponseDTO<PlaceDTO> response = placeService.getList(requestDTO, null);
        assertNotNull(response);
        System.out.println("카페(커피) 검색 결과 수: " + response.getTotal());
        assertTrue(response.getTotal() > 0, "카페(커피)로 검색 시 DB의 카페/디저트, 카페/베이커리가 조회되어야 합니다.");

        // 2. 한식 검색 테스트
        PageRequestDTO koreanDTO = PageRequestDTO.builder()
                .category("한식")
                .size(5)
                .build();
        PageResponseDTO<PlaceDTO> koreanResponse = placeService.getList(koreanDTO, null);
        assertNotNull(koreanResponse);
        System.out.println("한식 검색 결과 수: " + koreanResponse.getTotal());
        assertTrue(koreanResponse.getTotal() > 0);

        // 3. 가챠 뽑기 테스트 ("카페(커피)")
        GachaResponseDTO gacha = gachaService.drawRandomRestaurant(null, "카페(커피)");
        assertNotNull(gacha);
        System.out.println("가챠 뽑기 성공: " + gacha.getRestaurantNameKo() + " (" + gacha.getCategory() + ")");
    }
}
