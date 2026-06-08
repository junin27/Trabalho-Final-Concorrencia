package br.com.bancodigital.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * AccountTest covers the specifications for the Account model.
 * Adheres to TDD and FIRST principles.
 */
class AccountTest {

    @Test
    void shouldCreateAccountWithCorrectDetails() {
        Account account = new Account("123", "Gustavo", new BigDecimal("100.00"));
        assertEquals("123", account.getId());
        assertEquals("Gustavo", account.getOwner());
        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    @Test
    void shouldIncreaseBalanceOnValidDeposit() {
        Account account = new Account("123", "Gustavo", new BigDecimal("100.00"));
        account.deposit(new BigDecimal("50.00"));
        assertEquals(new BigDecimal("150.00"), account.getBalance());
    }

    @Test
    void shouldThrowExceptionOnNegativeDeposit() {
        Account account = new Account("123", "Gustavo", new BigDecimal("100.00"));
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.deposit(new BigDecimal("-10.00"))
        );
        assertEquals("Deposit amount must be positive. Provided: -10.00", exception.getMessage());
    }

    @Test
    void shouldDecreaseBalanceOnValidWithdraw() {
        Account account = new Account("123", "Gustavo", new BigDecimal("100.00"));
        account.withdraw(new BigDecimal("40.00"));
        assertEquals(new BigDecimal("60.00"), account.getBalance());
    }

    @Test
    void shouldThrowExceptionOnNegativeWithdraw() {
        Account account = new Account("123", "Gustavo", new BigDecimal("100.00"));
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(new BigDecimal("-5.00"))
        );
        assertEquals("Withdraw amount must be positive. Provided: -5.00", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionOnInsufficientBalance() {
        Account account = new Account("123", "Gustavo", new BigDecimal("50.00"));
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> account.withdraw(new BigDecimal("60.00"))
        );
        assertEquals("Insufficient balance. Account: 123, Balance: 50.00, Attempted: 60.00", exception.getMessage());
    }
}
