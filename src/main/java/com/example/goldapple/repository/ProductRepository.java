package com.example.goldapple.repository;

import com.example.goldapple.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByIdAsc();

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);
}
