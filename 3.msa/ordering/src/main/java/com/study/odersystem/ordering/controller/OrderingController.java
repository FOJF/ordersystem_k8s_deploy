package com.study.odersystem.ordering.controller;

import com.study.odersystem.common.dto.ResponseDto;
import com.study.odersystem.ordering.dto.OrderCreateDto;
import com.study.odersystem.ordering.dto.OrderingSpecificResDto;
import com.study.odersystem.ordering.service.OrderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderingController {
    private final OrderingService orderingService;

    @PostMapping("")
    public ResponseEntity<?> createOrder(@RequestBody List<OrderCreateDto> orderCreateDtos, @RequestHeader("X-User-Email") String email) {
//        OrderingSpecificResDto dto = this.orderingService.createOrder(orderCreateDtos, email);
        OrderingSpecificResDto dto = this.orderingService.createFeignKafka(orderCreateDtos, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ofSuccess(dto, HttpStatus.CREATED.value(), "주문 완료"));
    }

    @GetMapping("")
    public ResponseEntity<?> findAll() {
        List<OrderingSpecificResDto> dtos = this.orderingService.findAll();
        return ResponseEntity.ok().body(
                ResponseDto.ofSuccess(dtos, HttpStatus.OK.value(), "주문 목록 조회")
        );
    }

    @GetMapping("/my-list")
    public ResponseEntity<?> findMyList(@RequestHeader("X-User-Email") String email) {
        List<OrderingSpecificResDto> dtos = this.orderingService.findMyList(email);
        return ResponseEntity.ok().body(
                ResponseDto.ofSuccess(dtos, HttpStatus.OK.value(), "주문 목록 조회")
        );
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
//        Long canceledOrderingId = this.orderingService.cancelOrder(id);
//        return ResponseEntity.ok().body(
//                ResponseDto.ofSuccess(canceledOrderingId, HttpStatus.OK.value(), "주문 취소 완료")
//        );
//    }
}
