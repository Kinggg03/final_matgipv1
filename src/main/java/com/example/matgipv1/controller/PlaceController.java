package com.example.matgipv1.controller;

import com.example.matgipv1.domain.review.SoloLevel;
import com.example.matgipv1.service.PlaceService;
import com.example.matgipv1.service.ReviewService;
import dto.PageRequestDTO;
import dto.PageResponseDTO;
import dto.PlaceDTO;
import dto.ReviewDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/place")
@Log4j2
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final ReviewService reviewService;

    // 1. 목록 페이지 조회
    @GetMapping("/list")
    public void list(PageRequestDTO pageRequestDTO, Model model) {
        log.info("Place List GET... guName: " + pageRequestDTO.getGuName());
        PageResponseDTO<PlaceDTO> responseDTO = placeService.getList(pageRequestDTO);
        model.addAttribute("responseDTO", responseDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);
    }

    // 2. 등록 페이지 이동
    @GetMapping("/register")
    public void registerGET() {
        log.info("PlaceController registerGET() 호출");
    }

    // 3. 등록 처리
    @PostMapping("/register")
    public String registerPOST(PlaceDTO placeDTO, RedirectAttributes redirectAttributes) {
        log.info("PlaceController registerPOST() 호출: " + placeDTO);

        Long pno = placeService.register(placeDTO);
        redirectAttributes.addFlashAttribute("result", pno);

        return "redirect:/place/list";
    }

    // 4. 상세 조회 및 수정 페이지 이동 (상세조회 시 리뷰 목록 함께 조회)
    @GetMapping({"/read", "/modify"})
    public void read(Long pno, PageRequestDTO pageRequestDTO, Model model) {
        log.info("PlaceController read() 호출 - pno: " + pno);

        PlaceDTO placeDTO = placeService.readOne(pno);
        model.addAttribute("dto", placeDTO);

        // 해당 맛집의 리뷰 목록 조회하여 모델에 추가
        List<ReviewDTO> reviews = reviewService.getReviewsByRestaurant(pno);
        model.addAttribute("reviews", reviews);
    }

    // 5. 수정 처리
    @PostMapping("/modify")
    public String modify(PageRequestDTO pageRequestDTO, PlaceDTO placeDTO, RedirectAttributes redirectAttributes) {
        log.info("PlaceController modify() 호출: " + placeDTO);

        placeService.modify(placeDTO);
        redirectAttributes.addAttribute("pno", placeDTO.getPno());

        return "redirect:/place/read?" + pageRequestDTO.getLink();
    }

    // 6. 삭제 처리
    @PostMapping("/remove")
    public String remove(Long pno, RedirectAttributes redirectAttributes) {
        log.info("PlaceController remove() 호출 - pno: " + pno);

        placeService.remove(pno);
        redirectAttributes.addFlashAttribute("result", "removed");

        return "redirect:/place/list";
    }

    // 7. 리뷰 신규 등록 처리
    @PostMapping("/review/add")
    public String addReview(@RequestParam Long restaurantId,
                            @RequestParam(defaultValue = "5") Integer rating,
                            @RequestParam(defaultValue = "LV1") SoloLevel soloLevel,
                            @RequestParam String content,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Long memberNo = (Long) session.getAttribute("loginMember");
        if (memberNo == null) {
            redirectAttributes.addFlashAttribute("error", "로그인 후 리뷰를 작성할 수 있습니다.");
            return "redirect:/place/read?pno=" + restaurantId;
        }

        reviewService.addReview(restaurantId, memberNo, rating, soloLevel, content);
        redirectAttributes.addFlashAttribute("msg", "리뷰가 성공적으로 등록되었습니다.");

        return "redirect:/place/read?pno=" + restaurantId;
    }

    // 8. 리뷰 삭제 처리 (작성자 본인 또는 관리자)
    @PostMapping("/review/remove")
    public String removeReview(@RequestParam Long reviewId,
                               @RequestParam Long restaurantId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long memberNo = (Long) session.getAttribute("loginMember");
        String role = (String) session.getAttribute("loginRole");
        boolean isAdmin = "ROLE_ADMIN".equals(role);

        if (memberNo == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/place/read?pno=" + restaurantId;
        }

        boolean success = reviewService.deleteReview(reviewId, memberNo, isAdmin);
        if (success) {
            redirectAttributes.addFlashAttribute("msg", "리뷰가 삭제되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("error", "본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        return "redirect:/place/read?pno=" + restaurantId;
    }

    // 9. 전체 맛집 목록 JSON API (지도 실시간 마커 및 이동 연동용)
    @GetMapping("/api/places")
    @ResponseBody
    public List<PlaceDTO> getAllPlacesApi() {
        return placeService.getAllPlaces();
    }

    // http://localhost:8081/place/map 접속 시 처리
    @GetMapping("/map")
    public String map() {
        return "place/map";
    }

    // http://localhost:8081/place/gacha 접속 시 처리 (오늘 뭐 먹지? 가챠 전용 페이지)
    @GetMapping("/gacha")
    public String gacha() {
        return "place/gacha";
    }
}
