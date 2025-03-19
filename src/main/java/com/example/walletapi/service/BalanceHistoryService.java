package com.example.walletapi.service;

import com.example.walletapi.model.BalanceHistory;
import com.example.walletapi.model.Wallet;
import com.example.walletapi.repository.BalanceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceHistoryService {
    private final BalanceHistoryRepository historyRepo;

    public void recordBalance(Wallet wallet) {
        log.info("Recording balance history for userId={}, balance={}", wallet.getUserId(), wallet.getBalance());
        historyRepo.save(BalanceHistory.builder()
                        .userId(wallet.getUserId())
                        .balance(wallet.getBalance())
                        .build());
    }
}
