package com.study.odersystem.ordering.domain;

import com.study.odersystem.common.domain.BaseTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Ordering extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 관계성 끊고, 이메일(유니크)을 저장
    private String memberEmail;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    @Builder.Default
    @OneToMany(mappedBy = "ordering",  fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

//    public void updateOrderDetails(List<OrderDetail> orderDetails) {
//        this.orderDetails = orderDetails;
//    }

    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCELED;
    }
}
