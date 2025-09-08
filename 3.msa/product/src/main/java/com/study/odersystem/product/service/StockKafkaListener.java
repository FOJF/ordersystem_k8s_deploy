package com.study.odersystem.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.odersystem.product.dto.UpdateStockDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockKafkaListener {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "update-stock", containerFactory = "kafkaListener")
    public void stockConsumer(String message) {
        log.info("(consumer)message:{}", message);
        UpdateStockDto updateStockDto = null;
        try {
            updateStockDto = objectMapper.readValue(message, UpdateStockDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        productService.updateStock(updateStockDto);
    }
}
