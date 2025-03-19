package com.example.walletapi.controller;

import com.example.walletapi.model.BalanceHistory;
import com.example.walletapi.model.TransactionType;
import com.example.walletapi.model.Wallet;
import com.example.walletapi.repository.BalanceHistoryRepository;
import com.example.walletapi.repository.TransactionHistoryRepository;
import com.example.walletapi.repository.WalletRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private BalanceHistoryRepository balanceHistoryRepository;

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        balanceHistoryRepository.deleteAll();
        transactionHistoryRepository.deleteAll();
        userId = createWallet(1L, BigDecimal.ZERO);
    }

    @Test
    void createWallet_ShouldReturnWallet() throws Exception {
        mockMvc.perform(post("/api/wallet/{userId}", 9999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0));

        Assertions.assertThat(walletRepository.findByUserId(9999L)).isNotEmpty();
    }

    @Test
    void createWallet_AlreadyExistingWallet_ShouldThrowException() throws Exception {
        Assertions.assertThat(walletRepository.findByUserId(userId)).isNotEmpty();

        mockMvc.perform(post("/api/wallet/{userId}", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Wallet creation failed -> Wallet already exists for provided user"));
    }

    @Test
    void getBalance_ShouldReturnBalance() throws Exception {
        mockMvc.perform(get("/api/wallet/{userId}/balance", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("0.00"));
    }

    @Test
    void deposit_ShouldIncreaseBalance() throws Exception {
        validateEmptyHistoryState();

        mockMvc.perform(post("/api/wallet/{userId}/deposit", userId)
                        .param("amount", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deposit successful"));

        validateUpdatedHistoryState(userId, TransactionType.DEPOSIT);
    }

    @Test
    void withdraw_ShouldDecreaseBalance() throws Exception {
        validateEmptyHistoryState();
        updateWalletBalance(userId, BigDecimal.valueOf(200));

        mockMvc.perform(post("/api/wallet/{userId}/withdraw", userId)
                        .param("amount", "50"))
                .andExpect(status().isOk())
                .andExpect(content().string("Withdrawal successful"));

        validateUpdatedHistoryState(userId, TransactionType.WITHDRAWAL);
    }

    @Test
    void transfer_ShouldTransferBalance() throws Exception {
        validateEmptyHistoryState();
        Long toUserId = createWallet(2L, BigDecimal.ZERO);
        updateWalletBalance(userId, BigDecimal.valueOf(200));

        mockMvc.perform(post("/api/wallet/{fromUserId}/transfer/{toUserId}", userId, toUserId)
                        .param("amount", "50"))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer successful"));

        Assertions.assertThat(balanceHistoryRepository.findAll())
                .extracting(BalanceHistory::getUserId)
                .containsExactlyInAnyOrder(userId, toUserId);

        Assertions.assertThat(transactionHistoryRepository.findAll())
                .anyMatch(t -> t.getFromUserId().equals(userId) && t.getType() == TransactionType.TRANSFER);
    }

    private void validateUpdatedHistoryState(Long userToCheck, TransactionType typeToCheck) {
        Assertions.assertThat(balanceHistoryRepository.findAll())
                .anyMatch(b -> b.getUserId().equals(userToCheck));

        Assertions.assertThat(transactionHistoryRepository.findAll())
                .anyMatch(t -> t.getType() == typeToCheck);
    }

    private void validateEmptyHistoryState() {
        Assertions.assertThat(balanceHistoryRepository.findAll()).isEmpty();
        Assertions.assertThat(transactionHistoryRepository.findAll()).isEmpty();
    }

    private Long createWallet(Long userId, BigDecimal balance) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(balance);
        return walletRepository.save(wallet).getUserId();
    }

    private void updateWalletBalance(Long userId, BigDecimal newBalance) {
        walletRepository.findById(userId).ifPresent(wallet -> {
            wallet.setBalance(newBalance);
            walletRepository.save(wallet);
        });
    }
}
