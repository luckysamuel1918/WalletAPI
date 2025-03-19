package com.example.walletapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "balances_history")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BalanceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "timestamp", updatable = false, nullable = false)
    @CreationTimestamp
    private Instant timestamp;

}
