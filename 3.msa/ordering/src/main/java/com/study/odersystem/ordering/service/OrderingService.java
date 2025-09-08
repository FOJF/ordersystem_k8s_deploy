package com.study.odersystem.ordering.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.odersystem.common.dto.ProductDto;
import com.study.odersystem.common.dto.ResponseDto;
import com.study.odersystem.common.service.SseAlarmService;
import com.study.odersystem.ordering.domain.OrderDetail;
import com.study.odersystem.ordering.domain.OrderStatus;
import com.study.odersystem.ordering.domain.Ordering;
import com.study.odersystem.ordering.dto.OrderCreateDto;
import com.study.odersystem.ordering.dto.OrderingSpecificResDto;
import com.study.odersystem.ordering.feignclient.ProductFeignClient;
import com.study.odersystem.ordering.repository.OrderingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final SseAlarmService sseAlarmService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ProductFeignClient productFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // synchronized를 사용하더라도 mariaDB 자체도 멀티 쓰레드로 동작하기 때문에 여전히 문제가 발생할 소지가 있음
    public OrderingSpecificResDto createOrder(List<OrderCreateDto> dtos, String email) {
        Ordering ordering = Ordering.builder().memberEmail(email).build();

        dtos.forEach(dto -> {
            // 상품 조회
            String productDetailUrl = "http://product-service/product/" + dto.getProductId();
            HttpHeaders headers = new HttpHeaders();
            // headers.set("X-User-Email", email); // 여기서는 사용하지는 않지만 이런식으로 헤더 세팅
            // HttpEntity : HttpBody와 HttpHeader를 세팅하기 위한 객체
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<ResponseDto> responseEntity = null;
            try {
                responseEntity = restTemplate.exchange(productDetailUrl, HttpMethod.GET, httpEntity, ResponseDto.class);
            } catch (HttpClientErrorException.Forbidden e) {
                throw new EntityNotFoundException("Product not found");
            }

            ResponseDto response = responseEntity.getBody();

            log.info("response={}", response);

            // readValue : String -> 원하는 클래스, convertValue : Object -> 원하는 클래스
            ProductDto product = objectMapper.convertValue(response.getData(), ProductDto.class);
            log.info("product={}", product);
//            if (product.getStockQuantity() < dto.getProductCount()) throw new IllegalStateException("재고가 부족합니다.");

            // 주문 발생
            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .ordering(ordering)
                    .quantity(dto.getProductCount())
                    .build();

            ordering.getOrderDetails().add(orderDetail);

            // 동기적 재고 감소 요청
            String productUpdateStockUrl = "http://product-service/product/update-stock";
            HttpHeaders updateStockHeaders = new HttpHeaders();
            HttpEntity<OrderCreateDto> updateStockHttpEntity = new HttpEntity<>(dto, updateStockHeaders);
            try {
                restTemplate.exchange(productUpdateStockUrl, HttpMethod.PUT, updateStockHttpEntity, Void.class);
            } catch (HttpClientErrorException.BadRequest e) {
                throw new IllegalStateException("product stock quantity less than stock quantity");
            } catch (HttpClientErrorException.Forbidden e) {
                throw new EntityNotFoundException("Product not found");
            }
        });

        this.orderingRepository.save(ordering);
        this.sseAlarmService.publishOrderingMessage("admin", email, ordering.getId(), OrderStatus.ORDERED);
        return OrderingSpecificResDto.fromEntity(ordering);
    }

    // fallback메서드는 원본 메서드의 매개변수와 완벽히 일치해야 함.
    public void fallbackProductService(List<OrderCreateDto> dtos, String email, Throwable t) {
        throw new RuntimeException("상품 응답 없음, 잠시 후 다시 시도해주세요.");
    }

    // 테스트 : 4~5번의 정상요청 -> 5번 중에 2번의 지연을 발생시킴 -> circuit open -> 그 다음 요청은 바로 fallback
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackProductService")
    public OrderingSpecificResDto createFeignKafka(List<OrderCreateDto> dtos, String email) {
        Ordering ordering = Ordering.builder().memberEmail(email).build();

        dtos.forEach(dto -> {
//             feign client를 이용한 상품 조회

            ResponseDto responseDto = productFeignClient.getProductById(dto.getProductId());
            ProductDto product = objectMapper.convertValue(responseDto.getData(), ProductDto.class);
            if (product.getStockQuantity() < dto.getProductCount()) throw new IllegalStateException("재고가 부족합니다.");

            // 주문 발생
            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .ordering(ordering)
                    .quantity(dto.getProductCount())
                    .build();

            ordering.getOrderDetails().add(orderDetail);

            // Feign을 통한 동기적 재고 감소 요청
//            productFeignClient.updateProductStock(dto);


//             kafka를 활용한 비 동기적 재고 감소 요청
            kafkaTemplate.send("update-stock", dto);
        });

        this.orderingRepository.save(ordering);
        this.sseAlarmService.publishOrderingMessage("admin", email, ordering.getId(), OrderStatus.ORDERED);
        return OrderingSpecificResDto.fromEntity(ordering);
    }

    public List<OrderingSpecificResDto> findAll() {
        return this.orderingRepository.findAll().stream().map(OrderingSpecificResDto::fromEntity).toList();
    }

    public List<OrderingSpecificResDto> findMyList(String email) {
        return this.orderingRepository.findAllByMemberEmail(email).stream().map(OrderingSpecificResDto::fromEntity).toList();
    }
}
