package com.study.odersystem.member.dto;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDetailResDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Boolean deleted;

    public static MemberDetailResDto fromEntity(Member member) {
        return MemberDetailResDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .deleted(member.getDeleted())
                .build();
    }
}
