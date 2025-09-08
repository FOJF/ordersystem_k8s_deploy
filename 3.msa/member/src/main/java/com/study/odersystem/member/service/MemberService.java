package com.study.odersystem.member.service;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.dto.MemberCreateDto;
import com.study.odersystem.member.dto.MemberDetailResDto;
import com.study.odersystem.member.dto.LoginReqDto;
import com.study.odersystem.member.dto.MemberSummaryResDto;
import com.study.odersystem.member.repository.MemberRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberDetailResDto save(MemberCreateDto dto) {
        if (this.memberRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new EntityExistsException("중복된 이메일입니다.");
        }

        dto.setPassword(passwordEncoder.encode(dto.getPassword()));

        return MemberDetailResDto.fromEntity(this.memberRepository.save(dto.toEntity()));
    }

    public Member doLogin(LoginReqDto dto) {
        Member member = this.memberRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 틀렸습니다."));
        if (!this.passwordEncoder.matches(dto.getPassword(), member.getPassword()))
            throw new IllegalArgumentException("이메일 또는 비밀번호가 틀렸습니다.");
        if (member.getDeleted()) throw new EntityNotFoundException("삭제된 멤버입니다.");

        return member;
    }

    public MemberDetailResDto delete(String email) {
        Member member = this.memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 멤버"));
        member.delete();
        return MemberDetailResDto.fromEntity(member);
    }

    public List<MemberSummaryResDto> findAll() {
        return this.memberRepository.findAll().stream().map(MemberSummaryResDto::fromEntity).collect(Collectors.toList());
    }

    public MemberDetailResDto getMyInfo(String email) {
        Member member = this.memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 멤버"));
        return MemberDetailResDto.fromEntity(member);
    }

    public MemberDetailResDto findById(Long id) {
        return MemberDetailResDto.fromEntity(memberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 멤버")));
    }
}
