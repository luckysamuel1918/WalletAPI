package com.example.walletapi.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.walletapi.model.Wallet;
import com.example.walletapi.repository.WalletRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private Long userId;
    private Long toUserId;
    private Wallet wallet;
    private Wallet toWallet;

    @BeforeEach
    void setUp() {
        userId = 1L;
        toUserId = 2L;
        wallet = new Wallet();
        wallet.setBalance(BigDecimal.ZERO);
        toWallet = new Wallet();
        toWallet.setBalance(BigDecimal.ZERO);
    }

    @Test
    void createWallet_ShouldCreateNewWallet() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet createdWallet = walletService.createWallet(userId);

        assertNotNull(createdWallet);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createWallet_ShouldThrowException_WhenWalletAlreadyExists() {
        when(walletRepository.findByUserId(eq(userId))).thenReturn(Optional.of(wallet));

        Assertions.assertThatThrownBy(() -> walletService.createWallet(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wallet creation failed -> Wallet already exists for provided user");
    }

    @Test
    void getBalance_ShouldThrowException_WhenWalletNotFound() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> walletService.getBalance(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wallet not found.");
    }

    @Test
    void getBalance_ShouldReturnBalance() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        BigDecimal balance = walletService.getBalance(userId);
        assertEquals(BigDecimal.ZERO, balance);
    }

    @Test
    void deposit_ShouldThrowException_WhenWalletNotFound() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> walletService.deposit(userId, BigDecimal.valueOf(100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wallet not found.");
        verify(walletRepository, times(0)).save(any(Wallet.class));
    }

    @Test
    void deposit_ShouldIncreaseBalance() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet updatedWallet = walletService.deposit(userId, BigDecimal.valueOf(100));
        assertEquals(BigDecimal.valueOf(100), updatedWallet.getBalance());
    }

    @Test
    void withdraw_ShouldThrowException_WhenWalletNotFound() {
        wallet.setBalance(BigDecimal.valueOf(50));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        Assertions.assertThatThrownBy(() -> walletService.withdraw(userId, BigDecimal.valueOf(100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Balance withdraw failed -> Insufficient balance");

    }

    @Test
    void withdraw_ShouldThrowException_WhenInsufficientBalance() {
        wallet.setBalance(BigDecimal.valueOf(50));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        Assertions.assertThatThrownBy(() -> walletService.withdraw(userId, BigDecimal.valueOf(100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Balance withdraw failed -> Insufficient balance");

    }

    @Test
    void withdraw_ShouldDecreaseBalance() {
        wallet.setBalance(BigDecimal.valueOf(200));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet updatedWallet = walletService.withdraw(userId, BigDecimal.valueOf(50));
        assertEquals(BigDecimal.valueOf(150), updatedWallet.getBalance());
    }

    @Test
    void transfer_ShouldThrowException_WhenFromWalletNotFound() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> walletService.transfer(userId, toUserId, BigDecimal.valueOf(50)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wallet not found.");
    }

    @Test
    void transfer_ShouldThrowException_WhenToWalletNotFound() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.findByUserId(toUserId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> walletService.transfer(userId, toUserId, BigDecimal.valueOf(50)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wallet not found.");
    }

    @Test
    void transfer_ShouldThrowException_WhenInsufficientBalance() {
        wallet.setBalance(BigDecimal.valueOf(30));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.findByUserId(toUserId)).thenReturn(Optional.of(toWallet));

        Assertions.assertThatThrownBy(() -> walletService.transfer(userId, toUserId, BigDecimal.valueOf(50)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Balance transfer failed -> Insufficient origin balance");
    }

    @Test
    void transfer_ShouldTransferAmount() {
        wallet.setBalance(BigDecimal.valueOf(100));
        toWallet.setBalance(BigDecimal.valueOf(50));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.findByUserId(toUserId)).thenReturn(Optional.of(toWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Wallet> result = walletService.transfer(userId, toUserId, BigDecimal.valueOf(30));

        assertEquals(BigDecimal.valueOf(70), result.get("fromWallet").getBalance());
        assertEquals(BigDecimal.valueOf(80), result.get("toWallet").getBalance());
    }
}