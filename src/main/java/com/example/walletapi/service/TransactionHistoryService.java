package com.example.walletapi.service;

import com.example.walletapi.model.TransactionHistory;
import com.example.walletapi.model.TransactionType;
import com.example.walletapi.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionHistoryService {
    private final TransactionHistoryRepository transactionRepo;

    public void recordTransaction(Long fromUserId, BigDecimal amount, TransactionType transactionType, Long targetUserId) {
        log.info("Recording transaction history: fromUserId={}, amount={}, transactionType={}, targetUserId={}",
                fromUserId, amount, transactionType, targetUserId);
        transactionRepo.save(TransactionHistory.builder()
                        .fromUserId(fromUserId)
                        .type(transactionType)
                        .amount(amount)
                        .targetUserId(targetUserId)
                        .build());
    }
}
