package com.study.odersystem.ordering.service;

import com.study.odersystem.common.service.SseAlarmService;
import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.repository.MemberRepository;
import com.study.odersystem.ordering.domain.OrderDetail;
import com.study.odersystem.ordering.domain.OrderStatus;
import com.study.odersystem.ordering.domain.Ordering;
import com.study.odersystem.ordering.dto.OrderCreateDto;
import com.study.odersystem.ordering.dto.OrderingSpecificResDto;
import com.study.odersystem.ordering.repository.OrderingRepository;
import com.study.odersystem.product.domain.Product;
import com.study.odersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final SseAlarmService sseAlarmService;

    // synchronized를 사용하더라도 mariaDB 자체도 멀티 쓰레드로 동작하기 때문에 여전히 문제가 발생할 소지가 있음
    public OrderingSpecificResDto createOrder(List<OrderCreateDto> dtos) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = this.memberRepository.findByEmail(authentication.getName()).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Ordering ordering = Ordering.builder().member(member).build();

        dtos.forEach(dto -> {
            Product product = this.productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("Product not found"));

            // 동시에 접근하는 상황에서 update한 결과의 정합성이 깨지는 갱신이상(lost update) 발생의 소지가 있음
            // 스프링의 버전이나 mariadb의 버전에 따라 jpa에서 강제에러를 유발시켜 대부분의 요청이 실패(Rollback 처리)할 소지가 있음
            // -> 보통 발생하는 상황이다. 갱신이상은 발생하지 않지만 사용자 경험이 너무 떨어지는 문제가 발생할 수 있어서 역시 문제가 됨
            if (product.getStockQuantity() < dto.getProductCount()) throw new IllegalStateException("재고가 부족합니다.");
            product.decreaseStockQuantity(dto.getProductCount());

            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .ordering(ordering)
                    .quantity(dto.getProductCount())
                    .build();

            ordering.getOrderDetails().add(orderDetail);
        });

        this.orderingRepository.save(ordering);
        this.sseAlarmService.publishOrderingMessage("admin", member.getEmail(), ordering.getId(), OrderStatus.ORDERED);
        return OrderingSpecificResDto.fromEntity(ordering);
    }

    public List<OrderingSpecificResDto> findAll() {
        return this.orderingRepository.findAll().stream().map(OrderingSpecificResDto::fromEntity).toList();
    }

    public List<OrderingSpecificResDto> findMyList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = this.memberRepository.findByEmail(authentication.getName()).orElseThrow(() -> new EntityNotFoundException("User not found"));

        return this.orderingRepository.findAllByMember(member).stream().map(OrderingSpecificResDto::fromEntity).toList();
    }

    public Long cancelOrder(Long id) {
        Ordering ordering = this.orderingRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Order not found"));

        ordering.cancelOrder();

        ordering.getOrderDetails().forEach(orderDetail -> {
            Product product = this.productRepository.findById(orderDetail.getProduct().getId()).orElseThrow(() -> new EntityNotFoundException("Product not found"));

            product.increaseStockQuantity(orderDetail.getQuantity());
        });

        sseAlarmService.publishOrderingMessage(ordering.getMember().getEmail(), "admin", ordering.getId(), OrderStatus.CANCELED);

        return ordering.getId();
    }
}
