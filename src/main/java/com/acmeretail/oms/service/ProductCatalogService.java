package com.acmeretail.oms.service;

import com.acmeretail.oms.domain.model.Product;
import com.acmeretail.oms.exception.ResourceNotFoundException;
import com.acmeretail.oms.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductCatalogService {

    private final ProductRepository productRepository;

    public ProductCatalogService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> listActive() {
        return productRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Product getBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", sku));
    }
}
