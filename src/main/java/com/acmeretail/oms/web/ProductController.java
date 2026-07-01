package com.acmeretail.oms.web;

import com.acmeretail.oms.service.ProductCatalogService;
import com.acmeretail.oms.web.dto.ProductResponse;
import com.acmeretail.oms.web.mapper.ResponseMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductCatalogService productCatalogService;

    public ProductController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @GetMapping
    public List<ProductResponse> list() {
        return productCatalogService.listActive().stream()
                .map(ResponseMapper::toProductResponse)
                .toList();
    }

    @GetMapping("/{sku}")
    public ProductResponse get(@PathVariable String sku) {
        return ResponseMapper.toProductResponse(productCatalogService.getBySku(sku));
    }
}
