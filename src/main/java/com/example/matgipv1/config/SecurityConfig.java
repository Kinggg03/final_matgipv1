package com.example.matgipv1.config;

import com.example.matgipv1.domain.Member;
import com.example.matgipv1.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final MemberRepository memberRepository;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService
    ) throws Exception {

        http
                // 1. CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // 2. URL 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/place/**", "/member/**", "/admin/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 3. OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/member/login")
                        .successHandler(customOAuth2SuccessHandler)
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                )

                // 4. 예외 처리
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> request.getRequestURI().startsWith("/api/")
                        )
                );

        return http.build();
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 네이버 Token 요청시 state 추가 설정
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> authorizationCodeAccessTokenResponseClient() {
        RestClientAuthorizationCodeTokenResponseClient client = new RestClientAuthorizationCodeTokenResponseClient();

        client.addParametersConverter(grantRequest -> {
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            String registrationId = grantRequest.getClientRegistration().getRegistrationId();

            if ("naver".equals(registrationId)) {
                String state = grantRequest.getAuthorizationExchange().getAuthorizationRequest().getState();
                if (state != null && !state.isBlank()) {
                    parameters.add("state", state);
                }
            }
            return parameters;
        });

        return client;
    }

    // OAuth2 사용자 정보 처리 서비스
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        return userRequest -> {
            OAuth2User oAuth2User = delegate.loadUser(userRequest);
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            String providerId;
            String email;
            String nickname;

            // NAVER
            if ("naver".equals(registrationId)) {
                Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
                if (naverResponse == null) {
                    throw new IllegalStateException("네이버 response 데이터가 없습니다.");
                }

                providerId = String.valueOf(naverResponse.get("id"));
                Object emailObject = naverResponse.get("email");
                Object nicknameObject = naverResponse.get("nickname");

                email = emailObject != null ? String.valueOf(emailObject) : null;
                nickname = nicknameObject != null ? String.valueOf(nicknameObject) : "네이버사용자";

                if (email == null || email.isBlank()) {
                    email = "naver_" + providerId + "@naver.social";
                }
            }
            // KAKAO
            else if ("kakao".equals(registrationId)) {
                providerId = String.valueOf(attributes.get("id"));
                Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

                nickname = null;
                if (properties != null && properties.get("nickname") != null) {
                    nickname = String.valueOf(properties.get("nickname"));
                }
                if (nickname == null && kakaoAccount != null && kakaoAccount.get("profile") != null) {
                    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                    if (profile.get("nickname") != null) {
                        nickname = String.valueOf(profile.get("nickname"));
                    }
                }
                if (nickname == null || nickname.isBlank()) {
                    nickname = "카카오사용자";
                }

                email = null;
                if (kakaoAccount != null && kakaoAccount.get("email") != null) {
                    email = String.valueOf(kakaoAccount.get("email"));
                }
                if (email == null || email.isBlank()) {
                    email = "kakao_" + providerId + "@kakao.social";
                }
            } else {
                throw new IllegalStateException("지원하지 않는 OAuth Provider: " + registrationId);
            }

            // 회원 DB 저장 및 조회
            final String finalEmail = email;
            final String finalNickname = nickname;

            memberRepository.findByEmail(finalEmail)
                    .orElseGet(() -> memberRepository.save(
                            Member.builder()
                                    .email(finalEmail)
                                    .password("")
                                    .nickname(finalNickname)
                                    .role("ROLE_USER")
                                    .provider(registrationId.toUpperCase())
                                    .providerId(providerId)
                                    .build()
                    ));

            return oAuth2User;
        };
    }
}
