package com.walmart.challenge.service;

import com.walmart.challenge.dto.ProductRequest;
import com.walmart.challenge.dto.ProductResponse;
import com.walmart.challenge.entity.Product;
import com.walmart.challenge.entity.ProductDimension;
import com.walmart.challenge.entity.SizeCategory;
import com.walmart.challenge.repository.ProductDimensionRepository;
import com.walmart.challenge.repository.ProductRepository;
import com.walmart.challenge.repository.SizeCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductDimensionRepository productDimensionRepository;
    private final SizeCategoryRepository sizeCategoryRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) return List.of();

        // Optimized Batch Fetch for Dimensions (Avoids N+1)
        Set<UUID> productIds = products.stream().map(Product::getId).collect(Collectors.toSet());
        Map<UUID, ProductDimension> dimensionsMap = productDimensionRepository.findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(d -> d.getProduct().getId(), Function.identity()));

        // Fetch categories once for calculation
        List<SizeCategory> categories = sizeCategoryRepository.findAllByOrderByMinVolumeAsc();

        return products.stream()
                .map(p -> mapToResponse(p, dimensionsMap.get(p.getId()), categories))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(product -> {
                    ProductDimension dim = productDimensionRepository.findByProductId(product.getId()).orElse(null);
                    // Fetch categories only if dimension exists to calculate volume
                    List<SizeCategory> categories = (dim != null) ?
                            sizeCategoryRepository.findAllByOrderByMinVolumeAsc() : List.of();
                    return mapToResponse(product, dim, categories);
                });
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.findBySku(request.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product SKU already exists: " + request.getSku());
        }

        Product product = new Product();
        product.setSku(request.getSku());
        product.setUnitPrice(request.getUnitPrice());
        product = productRepository.save(product);

        ProductDimension dim = new ProductDimension();
        dim.setProduct(product);
        dim.setHeight(request.getHeight());
        dim.setWidth(request.getWidth());
        dim.setDepth(request.getDepth());
        productDimensionRepository.save(dim);

        log.info("Created product {}", product.getSku());
        return mapToResponse(product, dim, sizeCategoryRepository.findAllByOrderByMinVolumeAsc());
    }

    @Transactional
    public Optional<ProductResponse> updateProduct(String sku, ProductRequest request) {
        return productRepository.findBySku(sku).map(product -> {
            product.setUnitPrice(request.getUnitPrice());
            // SKU usually shouldn't change, but if your logic allows it:
            if (!product.getSku().equals(request.getSku())) {
                if (productRepository.findBySku(request.getSku()).isPresent()) {
                    throw new IllegalArgumentException("New SKU already exists");
                }
                product.setSku(request.getSku());
            }

            ProductDimension dim = productDimensionRepository.findByProductId(product.getId())
                    .orElseGet(() -> {
                        var newDim = new ProductDimension();
                        newDim.setProduct(product);
                        return newDim;
                    });

            dim.setHeight(request.getHeight());
            dim.setWidth(request.getWidth());
            dim.setDepth(request.getDepth());
            productDimensionRepository.save(dim);

            return mapToResponse(product, dim, sizeCategoryRepository.findAllByOrderByMinVolumeAsc());
        });
    }

    @Transactional
    public boolean deleteProduct(String sku) {
        return productRepository.findBySku(sku).map(product -> {
            productDimensionRepository.findByProductId(product.getId())
                    .ifPresent(productDimensionRepository::delete);
            productRepository.delete(product);
            log.info("Deleted product {}", sku);
            return true;
        }).orElse(false);
    }

    private ProductResponse mapToResponse(Product product, ProductDimension dim, List<SizeCategory> categories) {
        ProductResponse resp = new ProductResponse();
        resp.setId(product.getId());
        resp.setSku(product.getSku());
        resp.setUnitPrice(product.getUnitPrice());

        if (dim != null) {
            resp.setHeight(dim.getHeight());
            resp.setWidth(dim.getWidth());
            resp.setDepth(dim.getDepth());

            BigDecimal volume = dim.getHeight().multiply(dim.getWidth()).multiply(dim.getDepth());
            resp.setVolume(volume.setScale(4, RoundingMode.HALF_UP));
            resp.setSizeCategory(determineCategoryName(volume, categories));
        }
        return resp;
    }

    private String determineCategoryName(BigDecimal volume, List<SizeCategory> categories) {
        if (categories == null || categories.isEmpty()) return null;

        for (SizeCategory cat : categories) {
            boolean isAboveMin = volume.compareTo(cat.getMinVolume()) >= 0;
            boolean isBelowMax = cat.getMaxVolume() == null || volume.compareTo(cat.getMaxVolume()) <= 0;
            if (isAboveMin && isBelowMax) return cat.getName();
        }
        return categories.getLast().getName();
    }
}