package com.example.matgipv1.repository;

import com.example.matgipv1.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이메일로 회원 정보 조회
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
}
