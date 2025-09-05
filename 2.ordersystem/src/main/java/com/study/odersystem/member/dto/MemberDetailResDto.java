package com.study.odersystem.member.dto;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.domain.Role;
import com.study.odersystem.ordering.domain.Ordering;
import com.study.odersystem.ordering.dto.OrderingSpecificResDto;
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
    private List<OrderingSpecificResDto> orderings = new ArrayList<>();

    public static MemberDetailResDto fromEntity(Member member) {
        return MemberDetailResDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .deleted(member.getDeleted())
                .orderings(member.getOrderings().stream().map(OrderingSpecificResDto::fromEntity).toList())
                .build();
    }
}
