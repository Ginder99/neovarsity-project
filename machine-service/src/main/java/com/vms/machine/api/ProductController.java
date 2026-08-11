package com.vms.machine.api;

import com.vms.machine.dto.CreateProductRequest;
import com.vms.machine.dto.ProductResponse;
import com.vms.machine.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> addProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.addProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
