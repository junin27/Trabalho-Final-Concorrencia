package br.com.bancodigital.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Account represents a bank account.
 * Single Responsibility: Manage the details and state of a single bank account.
 */
public class Account {

    private final String id;
    private final String owner;
    private BigDecimal balance;

    public Account(String id, String owner, BigDecimal balance) {
        this.id = Objects.requireNonNull(id, "Account id must not be null");
        this.owner = Objects.requireNonNull(owner, "Account owner must not be null");
        this.balance = Objects.requireNonNull(balance, "Account initial balance must not be null");
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive. Provided: " + amount);
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be positive. Provided: " + amount);
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException(String.format(
                    "Insufficient balance. Account: %s, Balance: %s, Attempted: %s",
                    this.id, this.balance, amount));
        }
        this.balance = this.balance.subtract(amount);
    }
}
