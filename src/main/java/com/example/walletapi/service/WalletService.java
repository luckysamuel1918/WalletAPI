package com.example.walletapi.service;

import com.example.walletapi.model.Wallet;
import com.example.walletapi.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public Wallet createWallet(Long userId) {
        log.info("Attempting to create wallet for userId={}", userId);
        if (walletRepository.findByUserId(userId).isPresent()) {
            log.warn("Wallet creation failed -> Wallet already exists for userId={}", userId);
            throw new IllegalArgumentException("Wallet creation failed -> Wallet already exists for provided user");
        }
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet successfully created for userId={}, balance={}", savedWallet.getUserId(), savedWallet.getBalance());
        return savedWallet;
    }

    public BigDecimal getBalance(Long userId) {
        log.info("Fetching balance for userId={}", userId);
        BigDecimal balance = getOrElseThrow(userId).getBalance();
        log.info("Balance retrieved for userId={}, balance={}", userId, balance);
        return balance;
    }

    @Transactional
    public Wallet deposit(Long userId, BigDecimal amount) {
        log.info("Processing deposit: userId={}, amount={}", userId, amount);
        Wallet wallet = getOrElseThrow(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        Wallet updatedWallet = walletRepository.save(wallet);
        log.info("Deposit successful: userId={}, new balance={}", userId, updatedWallet.getBalance());
        return updatedWallet;
    }

    @Transactional
    public Wallet withdraw(Long userId, BigDecimal amount) {
        log.info("Processing withdrawal: userId={}, amount={}", userId, amount);
        Wallet wallet = getOrElseThrow(userId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            log.warn("Withdrawal failed -> Insufficient balance: userId={}, currentBalance={}, requestedAmount={}",
                    userId, wallet.getBalance(), amount);
            throw new IllegalArgumentException("Balance withdraw failed -> Insufficient balance");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        Wallet updatedWallet = walletRepository.save(wallet);
        log.info("Withdrawal successful: userId={}, new balance={}", userId, updatedWallet.getBalance());
        return updatedWallet;
    }

    @Transactional
    public Map<String, Wallet> transfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        log.info("Processing transfer: fromUserId={}, toUserId={}, amount={}", fromUserId, toUserId, amount);
        Wallet fromWallet = getOrElseThrow(fromUserId);
        Wallet toWallet = getOrElseThrow(toUserId);

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            log.warn("Transfer failed -> Insufficient balance: fromUserId={}, currentBalance={}, requestedAmount={}",
                    fromUserId, fromWallet.getBalance(), amount);
            throw new IllegalArgumentException("Balance transfer failed -> Insufficient origin balance");
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        log.info("Transfer successful: fromUserId={}, toUserId={}, transferredAmount={}, fromNewBalance={}, toNewBalance={}",
                fromUserId, toUserId, amount, fromWallet.getBalance(), toWallet.getBalance());

        return Map.of("fromWallet", fromWallet, "toWallet", toWallet);
    }

    private Wallet getOrElseThrow(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Wallet not found for userId={}", userId);
                    return new IllegalArgumentException("Wallet not found.");
                });
    }
}
