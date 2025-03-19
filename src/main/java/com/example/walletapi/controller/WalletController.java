package com.example.walletapi.controller;

import com.example.walletapi.model.TransactionType;
import com.example.walletapi.model.Wallet;
import com.example.walletapi.service.BalanceHistoryService;
import com.example.walletapi.service.TransactionHistoryService;
import com.example.walletapi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    private final BalanceHistoryService balanceHistoryService;
    private final TransactionHistoryService transactionHistoryService;

    @PostMapping("/{userId}")
    public ResponseEntity<Wallet> createWallet(@PathVariable Long userId) {
        log.info("Received request to create wallet for userId={}", userId);
        Wallet wallet = walletService.createWallet(userId);
        balanceHistoryService.recordBalance(wallet);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long userId) {
        log.info("Fetching balance for userId={}", userId);
        BigDecimal balance = walletService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{userId}/deposit")
    public ResponseEntity<String> deposit(@PathVariable Long userId, @RequestParam BigDecimal amount) {
        log.info("Received deposit request: userId={}, amount={}", userId, amount);
        Wallet wallet = walletService.deposit(userId, amount);
        balanceHistoryService.recordBalance(wallet);
        transactionHistoryService.recordTransaction(userId, amount, TransactionType.DEPOSIT, null);
        return ResponseEntity.ok("Deposit successful");
    }

    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<String> withdraw(@PathVariable Long userId, @RequestParam BigDecimal amount) {
        log.info("Received withdrawal request: userId={}, amount={}", userId, amount);
        Wallet wallet = walletService.withdraw(userId, amount);
        balanceHistoryService.recordBalance(wallet);
        transactionHistoryService.recordTransaction(userId, amount, TransactionType.WITHDRAWAL, null);
        return ResponseEntity.ok("Withdrawal successful");
    }

    @PostMapping("/{fromUserId}/transfer/{toUserId}")
    public ResponseEntity<String> transfer(@PathVariable Long fromUserId, @PathVariable Long toUserId, @RequestParam BigDecimal amount) {
        log.info("Received transfer request: fromUserId={}, toUserId={}, amount={}", fromUserId, toUserId, amount);
        Map<String, Wallet> walletsMap = walletService.transfer(fromUserId, toUserId, amount);
        walletsMap.values().forEach(balanceHistoryService::recordBalance);
        transactionHistoryService.recordTransaction(fromUserId, amount, TransactionType.TRANSFER, toUserId);
        return ResponseEntity.ok("Transfer successful");
    }
}
