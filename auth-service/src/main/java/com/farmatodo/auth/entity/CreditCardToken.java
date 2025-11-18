package com.farmatodo.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credit_card_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardToken {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String lastFourDigits;

    @Column(nullable = false)
    private String maskedCardNumber;

    @Column(nullable = false)
    private String expirationDate;

    @Column(nullable = false)
    private String cardHolderName;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private UUID transactionId;
}

