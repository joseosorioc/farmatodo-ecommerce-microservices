package com.farmatodo.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_searches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearch {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String searchQuery;

    @Column
    private UUID customerId;

    @Column(nullable = false)
    private LocalDateTime searchedAt;

    @Column
    private Integer resultsCount;
}

