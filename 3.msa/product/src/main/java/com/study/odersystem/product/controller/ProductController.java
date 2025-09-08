package com.study.odersystem.product.controller;

import com.study.odersystem.common.dto.ResponseDto;
import com.study.odersystem.product.dto.*;
import com.study.odersystem.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
//    private int tried = 0; // circuit breaker 테스트 용

    @PostMapping(path = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> save(@ModelAttribute ProductCreateDto productCreateDto, @RequestHeader("X-User-Email") String email) {
        Long id = this.productService.save(productCreateDto, email);
        return ResponseEntity.ok().body(ResponseDto.ofSuccess(id, HttpStatus.OK.value(), "Product saved successfully"));// circuit breaker 테스트 용 sleep
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseDto<Page<ProductSummaryResDto>>> findAll(Pageable pageable, ProductSearchDto productSearchDto) {
        log.info(pageable.toString());
        log.info(productSearchDto.toString());
        return ResponseEntity.ok(
                ResponseDto.ofSuccess(
                        this.productService.findAll(pageable, productSearchDto),
                        HttpStatus.OK.value(),
                        "Product list successfully")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<ProductDetailResDto>> findById(@PathVariable Long id) throws InterruptedException {
//        if (tried > 5 && tried < 8) {
//            Thread.sleep(3000L); // circuit breaker 테스트 용 sleep
//        }
//        tried++;
        ProductDetailResDto dto = this.productService.findById(id);

        return ResponseEntity.ok(ResponseDto.ofSuccess(dto, HttpStatus.OK.value(), "Product detail successfully"));
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<?> update(@PathVariable Long id, @ModelAttribute ProductUpdateDto productUpdateDto) {
//        ProductSummaryResDto dto = this.productService.updateProductDetail(id, productUpdateDto);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(
//                ResponseDto.ofSuccess(dto, HttpStatus.CREATED.value(), "상품 정보가 수정되었습니다.")
//        );
//    }

    @PutMapping("/update-stock")
    public ResponseEntity<?> updateStock(@RequestBody UpdateStockDto updateStockDto) {
        Long id = this.productService.updateStock(updateStockDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseDto.ofSuccess(id, HttpStatus.CREATED.value(), "재고 수량 변경 완료")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        ProductSummaryResDto dto = this.productService.deleteProduct(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseDto.ofSuccess(dto, HttpStatus.OK.value(), "상품 정보가 수정되었습니다.")
        );
    }
}
