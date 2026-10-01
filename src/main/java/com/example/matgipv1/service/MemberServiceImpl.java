package com.example.matgipv1.service;


import com.example.matgipv1.domain.Member;
import com.example.matgipv1.repository.MemberRepository;
import dto.MemberJoinDTO;
import dto.MemberLoginDTO;
import dto.MyPageDTO;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService, UserDetailsService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;


    // ==========================
    // 회원가입
    // ==========================

    @Override
    public Long join(MemberJoinDTO dto) {

        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        Member member = Member.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .role("ROLE_USER")
                .build();

        return memberRepository.save(member).getMno();
    }


    // ==========================
    // 로그인
    // ==========================

    @Override
    @Transactional(readOnly = true)
    public Member login(MemberLoginDTO dto) {

        return memberRepository.findByEmail(dto.getEmail())
                .filter(member ->
                        passwordEncoder.matches(
                                dto.getPassword(),
                                member.getPassword()
                        )
                )
                .orElse(null);
    }


    // ==========================
    // 마이페이지 조회
    // ==========================

    @Override
    @Transactional(readOnly = true)
    public MyPageDTO getMyPage(Long mno) {

        Member member = memberRepository.findById(mno)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원 정보가 없습니다.")
                );

        return MyPageDTO.builder()
                .mno(member.getMno())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .build();
    }


    // ==========================
    // 회원정보 수정
    // ==========================

    @Override
    public void modify(
            Long mno,
            String nickname,
            String newPassword,
            String profileImage
    ) {

        Member member = memberRepository.findById(mno)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원 정보가 없습니다.")
                );


        // 닉네임 / 프로필 수정
        member.changeProfile(
                nickname,
                profileImage
        );


        // 비밀번호를 입력했을 때만 변경
        if (newPassword != null && !newPassword.isBlank()) {

            String encodedPassword =
                    passwordEncoder.encode(newPassword);

            member.changePassword(encodedPassword);
        }

        // save()를 안 해도 됨.
        // @Transactional + JPA 변경감지로 UPDATE 됨.
    }


    // ==========================
    // 회원탈퇴
    // ==========================

    @Override
    public void withdraw(Long mno) {

        Member member = memberRepository.findById(mno)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원 정보가 없습니다.")
                );

        memberRepository.delete(member);
    }


    // ==========================
    // 이메일로 회원 찾기
    // ==========================

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> findByEmail(String email) {

        return memberRepository.findByEmail(email);
    }


    // ==========================
    // Spring Security 로그인
    // ==========================

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "사용자를 찾을 수 없습니다: " + email
                        )
                );

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .authorities(
                        Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                )
                .build();
    }
}