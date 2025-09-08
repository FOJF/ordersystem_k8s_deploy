package com.study.odersystem.member.domain;

import com.study.odersystem.common.domain.BaseTime;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false") // jpql을 제외하고 모든 조회 쿼리에 where deleted = false를 하는 효과
public class Member extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String name;
    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private Boolean deleted = Boolean.FALSE;

    public void delete() {
        this.deleted = Boolean.TRUE;
    }
}
