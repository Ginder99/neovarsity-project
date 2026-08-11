package com.vms.machine.service;

import com.vms.machine.dto.CreateMachineRequest;
import com.vms.machine.dto.CreateProductRequest;
import com.vms.machine.dto.ProductResponse;
import com.vms.machine.entity.Product;
import com.vms.machine.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

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

    public void bulkLoad() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-data/products_test_data.csv");

        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader() // reads first row as column names
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                String name = record.get("name");
                String description = record.get("description");
                String category = record.get("category");
                String imageUrl = record.get("image_url");
                double basePrice = Double.parseDouble(record.get("base_price"));
                addProduct(new CreateProductRequest(name, description, category, imageUrl, new BigDecimal(basePrice)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
