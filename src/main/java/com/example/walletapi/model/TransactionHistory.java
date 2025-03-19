package com.example.walletapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions_history")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class TransactionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "timestamp", updatable = false, nullable = false)
    @CreationTimestamp
    private Instant timestamp;
}

