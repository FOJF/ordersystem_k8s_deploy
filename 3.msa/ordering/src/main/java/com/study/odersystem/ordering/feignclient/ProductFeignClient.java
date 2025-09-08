package com.study.odersystem.ordering.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.study.odersystem.common.dto.ResponseDto;
import com.study.odersystem.ordering.dto.OrderCreateDto;

// name : eureka에 등록한 application.name을 의미
@FeignClient(name = "product-service", url="http://fojf-product-service")
public interface ProductFeignClient {
    @GetMapping("/product/{id}")
    ResponseDto getProductById(@PathVariable Long id);

    @PutMapping("/product/update-stock")
    void updateProductStock(@RequestBody OrderCreateDto orderCreateDto);
}
