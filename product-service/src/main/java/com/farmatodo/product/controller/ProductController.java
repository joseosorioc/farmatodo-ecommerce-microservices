package com.farmatodo.product.controller;

import com.farmatodo.product.dto.ProductResponse;
import com.farmatodo.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping("/ping")
    public ResponseEntity<Object> ping() {
        return ResponseEntity.ok(java.util.Map.of("message", "pong"));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam String query,
            @RequestParam(required = false) UUID customerId) {
        List<ProductResponse> products = productService.searchProducts(query, customerId);
        return ResponseEntity.ok(products);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
}

