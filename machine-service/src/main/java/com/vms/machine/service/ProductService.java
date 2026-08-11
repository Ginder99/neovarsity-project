package com.vms.machine.service;

import com.vms.machine.dto.CreateProductRequest;
import com.vms.machine.dto.ProductResponse;
import com.vms.machine.entity.Product;
import com.vms.machine.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse addProduct(CreateProductRequest request) {
        Product product = new Product(request.name(), request.description(),
                request.category(), request.imageUrl(), request.basePrice());
        Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }
}
