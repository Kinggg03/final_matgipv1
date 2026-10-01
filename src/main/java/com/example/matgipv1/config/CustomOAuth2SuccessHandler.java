package com.example.matgipv1.config;

import com.example.matgipv1.domain.Member;
import com.example.matgipv1.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Log4j2
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = null;

        // Naver
        if (attributes.get("response") != null) {
            Map<String, Object> naverResp = (Map<String, Object>) attributes.get("response");
            email = (String) naverResp.get("email");
            if (email == null) {
                email = "naver_" + naverResp.get("id") + "@naver.social";
            }
        }
        // Kakao
        else if (attributes.get("id") != null) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null && kakaoAccount.get("email") != null) {
                email = (String) kakaoAccount.get("email");
            }
            if (email == null) {
                email = "kakao_" + attributes.get("id") + "@kakao.social";
            }
        }

        if (email != null) {
            Member member = memberRepository.findByEmail(email).orElse(null);
            if (member != null) {
                HttpSession session = request.getSession();
                session.setAttribute("loginMember", member.getMno());
                session.setAttribute("nickname", member.getNickname());
                session.setAttribute("loginRole", member.getRole());
                log.info("OAuth2 Login Success -> Session Set: " + member.getEmail());
            }
        }

        response.sendRedirect("/place/list");
    }
}
