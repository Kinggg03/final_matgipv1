package com.example.matgipv1.controller;

import com.example.matgipv1.service.PlaceService;
import dto.PageRequestDTO;
import dto.PageResponseDTO;
import dto.PlaceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/place")
@Log4j2
@RequiredArgsConstructor

public class PlaceController {
    private final PlaceService placeService;

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

    // 4. 상세 조회 및 수정 페이지 이동
    @GetMapping({"/read", "/modify"})
    public void read(Long pno, PageRequestDTO pageRequestDTO, Model model) {
        log.info("PlaceController read() 호출 - pno: " + pno);

        PlaceDTO placeDTO = placeService.readOne(pno);
        model.addAttribute("dto", placeDTO);
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

    // http://localhost:8081/place/map 접속 시 처리
    @GetMapping("/map")
    public String map() {
        return "place/map";
    }
}
