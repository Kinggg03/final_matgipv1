package com.example.matgipv1.controller;

import com.example.matgipv1.domain.Member;
import com.example.matgipv1.service.LogDataService;
import com.example.matgipv1.service.MemberService;
import dto.MemberJoinDTO;
import dto.MemberLoginDTO;
import dto.MyPageDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/member")
@Log4j2
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final LogDataService logDataService;

    // 1. 회원가입 화면
    @GetMapping("/register")
    public void registerGET() {
    }

    // 2. 회원가입 처리
    @PostMapping("/register")
    public String registerPOST(MemberJoinDTO memberJoinDTO, HttpServletRequest request, Model model) {
        try {
            memberService.join(memberJoinDTO);
            logDataService.logActivity(request, "REGISTER", "일반 회원가입 성공: " + memberJoinDTO.getEmail(), null, null, null, "email=" + memberJoinDTO.getEmail());
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            logDataService.logActivity(request, "REGISTER_FAILED", "회원가입 실패: " + e.getMessage(), null, null, null, "email=" + memberJoinDTO.getEmail());
            return "member/register";
        }
        return "redirect:/member/login";
    }

    // 3. 로그인 화면
    @GetMapping("/login")
    public void loginGET() {
    }

    // 4. 로그인 처리 (일반 세션 로그인)
    @PostMapping("/login")
    public String loginPOST(MemberLoginDTO memberLoginDTO, HttpServletRequest request, HttpSession session, Model model) {
        try {
            Member member = memberService.login(memberLoginDTO);

            log.info("로그인 성공 회원: " + member.getEmail() + " [Role: " + member.getRole() + "]");

            session.setAttribute("loginMember", member.getMno());
            session.setAttribute("nickname", member.getNickname());
            session.setAttribute("loginRole", member.getRole());

            // 📝 일반 로그인 로그 기록
            logDataService.logActivity(request, "LOGIN_LOCAL", "일반 로그인 성공 (" + member.getEmail() + ")", member.getMno(), member.getNickname(), null, "role=" + member.getRole());

            // 관리자 계정(ROLE_ADMIN) 로그인 시 관리자 센터로 이동
            if ("ROLE_ADMIN".equals(member.getRole())) {
                return "redirect:/admin/index";
            }

            return "redirect:/place/list";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            logDataService.logActivity(request, "LOGIN_FAILED", "일반 로그인 실패 (" + memberLoginDTO.getEmail() + "): " + e.getMessage(), null, null, null, "email=" + memberLoginDTO.getEmail());
            return "member/login";
        }
    }

    // 5. 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpSession session) {
        Long mno = (Long) session.getAttribute("loginMember");
        String nickname = (String) session.getAttribute("nickname");
        logDataService.logActivity(request, "LOGOUT", "사용자 로그아웃", mno, nickname, null, null);

        session.invalidate();
        return "redirect:/place/list";
    }

    // 6. 마이페이지
    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model) {
        Long mno = (Long) session.getAttribute("loginMember");

        if (mno == null) {
            return "redirect:/member/login";
        }

        MyPageDTO dto = memberService.getMyPage(mno);
        model.addAttribute("dto", dto);

        return "member/mypage";
    }

    // 7. 회원 정보 수정
    @PostMapping("/modify")
    public String modify(
            HttpServletRequest request,
            HttpSession session,
            @RequestParam String nickname,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String profileImage
    ) {
        Long mno = (Long) session.getAttribute("loginMember");

        if (mno == null) {
            return "redirect:/member/login";
        }

        memberService.modify(mno, nickname, newPassword, profileImage);
        session.setAttribute("nickname", nickname);

        logDataService.logActivity(request, "MODIFY_PROFILE", "회원정보(닉네임/비밀번호) 수정", mno, nickname, null, "newNickname=" + nickname);

        return "redirect:/member/mypage";
    }

    // 8. 회원 탈퇴
    @PostMapping("/withdraw")
    public String withdraw(HttpServletRequest request, HttpSession session) {
        Long mno = (Long) session.getAttribute("loginMember");
        String nickname = (String) session.getAttribute("nickname");

        if (mno == null) {
            return "redirect:/member/login";
        }

        logDataService.logActivity(request, "WITHDRAW", "회원 자진 탈퇴", mno, nickname, null, null);
        memberService.withdraw(mno);
        session.invalidate();

        return "redirect:/place/list";
    }
}
