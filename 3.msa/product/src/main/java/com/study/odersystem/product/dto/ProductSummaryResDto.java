package com.study.odersystem.product.dto;

import com.study.odersystem.product.domain.Product;
import com.study.odersystem.product.domain.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductSummaryResDto {
    private Long id;
    private String name;
    private Integer price;
    private Integer stockQuantity;
    private String imageUrl;

    public static ProductSummaryResDto fromEntity(Product product) {
        String url = product.getProductImages().isEmpty() ? null : product.getProductImages().get(0).getUrl();
        return ProductSummaryResDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(url)
                .build();
    }
}
